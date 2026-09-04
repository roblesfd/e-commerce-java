package org.fernandodev.ecommerce.search.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.search.application.port.in.IndexUseCases;
import org.fernandodev.ecommerce.search.application.port.out.CatalogPort;
import org.fernandodev.ecommerce.search.application.port.out.InventoryPort;
import org.fernandodev.ecommerce.search.application.port.out.PricingPort;
import org.fernandodev.ecommerce.search.application.port.out.SearchDocumentRepository;
import org.fernandodev.ecommerce.search.domain.SearchDocument;
import org.fernandodev.ecommerce.search.domain.value_object.IndexStatus;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de mantenimiento del índice de búsqueda.
 * <p>
 * Construye el documento denormalizado a partir de Catalog, Pricing e Inventory
 * (modelo de lectura; el catálogo sigue siendo la fuente de verdad). Los fallos
 * de indexación se registran en el documento ({@link IndexStatus#FAILED}) para
 * su reintento.
 * </p>
 */
@RequiredArgsConstructor
public class IndexService implements IndexUseCases {

    private final SearchDocumentRepository index;
    private final CatalogPort catalog;
    private final PricingPort pricing;
    private final InventoryPort inventory;

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public SearchDocument indexProduct(ProductId productId) {
        try {
            CatalogPort.ProductInfo product = catalog.findProduct(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productId));
            if (product.deleted() || !product.active()) {
                index.delete(productId);
                return null;
            }
            String attributesText = attributesText(product.attributes());
            String searchableText = searchableText(product);
            boolean available = isAvailable(product);
            SearchDocument document = SearchDocument.create(
                    product.id(), product.name(), product.description(), product.sku(), product.barcode(),
                    attributesText, product.categoryIds(), product.brandId(),
                    pricing.currentPrice(productId, null), available, product.active(), searchableText,
                    Instant.now());
            document.markSynced(Instant.now());
            return index.save(document);
        } catch (RuntimeException e) {
            SearchDocument existing = index.findById(productId).orElse(null);
            if (existing == null) {
                existing = SearchDocument.create(productId, "desconocido", null, null, null, null,
                        Set.of(), null, null, false, false, "", Instant.now());
            }
            existing.markFailed(truncateError(e), Instant.now());
            return index.save(existing);
        }
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public void removeProduct(ProductId productId) {
        index.delete(productId);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public void rebuild() {
        catalog.listAllProductIds().forEach(this::indexProduct);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public SearchDocument retryIndexing(ProductId productId) {
        SearchDocument existing = index.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no indexado: " + productId));
        if (existing.getIndexStatus() != IndexStatus.FAILED) {
            return existing;
        }
        return indexProduct(productId);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public List<SearchDocument> listIndexFailures() {
        return index.findByIndexStatus(IndexStatus.FAILED);
    }

    private boolean isAvailable(CatalogPort.ProductInfo product) {
        if (inventory.availableStock(product.id(), null) > 0) {
            return true;
        }
        return product.variants().stream()
                .map(CatalogPort.VariantInfo::id)
                .anyMatch(variantId -> inventory.availableStock(product.id(), variantId) > 0);
    }

    private String attributesText(List<CatalogPort.AttributeInfo> attributes) {
        return attributes.stream()
                .map(attribute -> attribute.name() + ":" + attribute.value())
                .collect(Collectors.joining(" "));
    }

    private String searchableText(CatalogPort.ProductInfo product) {
        StringBuilder text = new StringBuilder();
        append(text, product.name());
        append(text, product.description());
        append(text, product.sku());
        append(text, product.barcode());
        append(text, attributesText(product.attributes()));
        product.variants().forEach(variant -> {
            append(text, variant.sku());
            append(text, variant.barcode());
        });
        return text.toString().toLowerCase();
    }

    private void append(StringBuilder text, String value) {
        if (value != null && !value.isBlank()) {
            if (!text.isEmpty()) {
                text.append(' ');
            }
            text.append(value.trim());
        }
    }

    private String truncateError(RuntimeException e) {
        String message = Objects.requireNonNullElse(e.getMessage(), e.getClass().getSimpleName());
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}