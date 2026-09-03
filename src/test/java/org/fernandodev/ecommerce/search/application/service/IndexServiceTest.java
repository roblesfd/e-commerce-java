package org.fernandodev.ecommerce.search.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.search.application.dto.SearchQuery;
import org.fernandodev.ecommerce.search.application.dto.SearchResults;
import org.fernandodev.ecommerce.search.application.port.out.CatalogPort;
import org.fernandodev.ecommerce.search.application.port.out.InventoryPort;
import org.fernandodev.ecommerce.search.application.port.out.PricingPort;
import org.fernandodev.ecommerce.search.application.port.out.SearchDocumentRepository;
import org.fernandodev.ecommerce.search.domain.SearchDocument;
import org.fernandodev.ecommerce.search.domain.value_object.IndexStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;

class IndexServiceTest {

    private final FakeSearchRepository index = new FakeSearchRepository();
    private final FakeCatalogPort catalog = new FakeCatalogPort();
    private final FakePricingPort pricing = new FakePricingPort();
    private final FakeInventoryPort inventory = new FakeInventoryPort();
    private final IndexService service = new IndexService(index, catalog, pricing, inventory);

    @Test
    void indexProductConstruyeDocumento() {
        ProductId productId = ProductId.generate();
        catalog.product = Optional.of(product(productId, true, false));
        inventory.stocks.put("product:" + productId.value(), 5);

        SearchDocument document = service.indexProduct(productId);

        assertThat(document.getIndexStatus()).isEqualTo(IndexStatus.SYNCED);
        assertThat(document.getName()).isEqualTo("Camisa");
        assertThat(document.getPrice()).isEqualTo(usd(900));
        assertThat(document.isAvailable()).isTrue();
        assertThat(document.getSearchableText()).contains("camisa").contains("sku-1");
    }

    @Test
    void indexProductDisponibleSiVarianteConStock() {
        ProductId productId = ProductId.generate();
        catalog.product = Optional.of(product(productId, true, false));
        inventory.stocks.put("variant:" + lastVariantId.value(), 5);

        SearchDocument document = service.indexProduct(productId);

        assertThat(document.isAvailable()).isTrue();
    }

    @Test
    void indexProductDeProductoBorradoElimina() {
        ProductId productId = ProductId.generate();
        index.docs.put(productId, SearchDocument.create(productId, "Viejo", null, null, null, null,
                Set.of(), null, null, false, true, "viejo", Instant.parse("2026-01-01T00:00:00Z")));
        catalog.product = Optional.of(product(productId, true, true));

        service.indexProduct(productId);

        assertThat(index.findById(productId)).isEmpty();
    }

    @Test
    void indexProductConFalloMarcaFailed() {
        ProductId productId = ProductId.generate();
        pricing.failure = true;

        SearchDocument document = service.indexProduct(productId);

        assertThat(document.getIndexStatus()).isEqualTo(IndexStatus.FAILED);
        assertThat(document.getLastError()).isNotBlank();
        assertThat(document.getAttempts()).isEqualTo(1);
    }

    @Test
    void rebuildIndexaTodosLosProductos() {
        catalog.productIds = List.of(ProductId.generate(), ProductId.generate(), ProductId.generate());
        catalog.product = Optional.of(product(catalog.productIds.get(0), true, false));

        service.rebuild();

        assertThat(catalog.indexed).isEqualTo(3);
    }

    @Test
    void retryIndexingReindexaElFallido() {
        ProductId productId = ProductId.generate();
        catalog.product = Optional.of(product(productId, true, false));
        pricing.failure = true;
        service.indexProduct(productId);
        assertThat(index.docs.get(productId).getIndexStatus()).isEqualTo(IndexStatus.FAILED);

        pricing.failure = false;
        SearchDocument retried = service.retryIndexing(productId);

        assertThat(retried.getIndexStatus()).isEqualTo(IndexStatus.SYNCED);
    }

    @Test
    void removeProductEliminaElDocumento() {
        ProductId productId = ProductId.generate();
        index.docs.put(productId, SearchDocument.create(productId, "A", null, null, null, null,
                Set.of(), null, null, false, true, "a", Instant.parse("2026-01-01T00:00:00Z")));

        service.removeProduct(productId);

        assertThat(index.findById(productId)).isEmpty();
    }

    private ProductVariantId lastVariantId;

    private CatalogPort.ProductInfo product(ProductId productId, boolean active, boolean deleted) {
        ProductVariantId variant = ProductVariantId.generate();
        lastVariantId = variant;
        return new CatalogPort.ProductInfo(productId, "Camisa", "Camisa de algodón", "SKU-1", "BC-1",
                Set.of(CategoryId.generate()), UUID.randomUUID(), active, deleted,
                List.of(new CatalogPort.VariantInfo(variant, "SKU-1-S", "BC-1-S")),
                List.of(new CatalogPort.AttributeInfo("color", "blanco")));
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }

    private static final class FakeSearchRepository implements SearchDocumentRepository {

        private final Map<ProductId, SearchDocument> docs = new HashMap<>();

        @Override
        public SearchDocument save(SearchDocument document) {
            docs.put(document.getProductId(), document);
            return document;
        }

        @Override
        public Optional<SearchDocument> findById(ProductId productId) {
            return Optional.ofNullable(docs.get(productId));
        }

        @Override
        public List<SearchDocument> findByIndexStatus(IndexStatus status) {
            return docs.values().stream().filter(d -> d.getIndexStatus() == status).toList();
        }

        @Override
        public void delete(ProductId productId) {
            docs.remove(productId);
        }

        @Override
        public List<ProductId> findAllProductIds() {
            return new ArrayList<>(docs.keySet());
        }

        @Override
        public SearchResults<SearchDocument> search(SearchQuery query) {
            return new SearchResults<>(List.copyOf(docs.values()), docs.size(), query.page(), query.size());
        }

        @Override
        public List<String> findAutocompleteNames(String prefix, int limit) {
            return List.of();
        }

        @Override
        public List<String> findSuggestions(String q, int limit) {
            return List.of();
        }
    }

    private static final class FakeCatalogPort implements CatalogPort {
        private Optional<ProductInfo> product = Optional.empty();
        private List<ProductId> productIds = List.of();
        private int indexed;

        @Override
        public Optional<ProductInfo> findProduct(ProductId productId) {
            indexed++;
            return product;
        }

        @Override
        public List<ProductId> listAllProductIds() {
            return productIds;
        }
    }

    private static final class FakePricingPort implements PricingPort {
        private boolean failure;

        @Override
        public Money currentPrice(ProductId productId, ProductVariantId variantId) {
            if (failure) {
                throw new IllegalStateException("pricing caído");
            }
            return usd(900);
        }
    }

    private static final class FakeInventoryPort implements InventoryPort {
        private final Map<String, Integer> stocks = new HashMap<>();

        @Override
        public int availableStock(ProductId productId, ProductVariantId variantId) {
            String key = variantId == null ? "product:" + productId.value() : "variant:" + variantId.value();
            return stocks.getOrDefault(key, 0);
        }
    }
}