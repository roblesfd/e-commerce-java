package org.fernandodev.ecommerce.search.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.search.application.dto.SearchQuery;
import org.fernandodev.ecommerce.search.application.dto.SearchResults;
import org.fernandodev.ecommerce.search.application.port.out.SearchDocumentRepository;
import org.fernandodev.ecommerce.search.domain.SearchDocument;
import org.fernandodev.ecommerce.search.domain.value_object.IndexStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaSearchDocumentRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private SearchDocumentRepository repository;

    private static final Instant AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void buscaPorTextoEnElDocumentoIndexado() {
        repository.save(documento("Camisa", "camisa camisa de algodón", "SKU-1"));
        repository.save(documento("Pantalón", "pantalón vaquero", "SKU-2"));

        SearchResults<SearchDocument> results = repository.search(
                new SearchQuery("algodón", null, null, null, null, null, null, SearchQuery.SortBy.RELEVANCE, 0, 10));

        assertThat(results.total()).isEqualTo(1);
        assertThat(results.items().get(0).getName()).isEqualTo("Camisa");
    }

    @Test
    void filtraPorCategoriaMarcaYRangoDePrecio() {
        CategoryId category = CategoryId.generate();
        UUID brand = UUID.randomUUID();
        repository.save(SearchDocument.create(ProductId.generate(), "Camisa", null, "SKU-1", null, null,
                Set.of(category), brand, Money.of("900", "USD"), true, true, "camisa", AT));
        repository.save(SearchDocument.create(ProductId.generate(), "Zapatos", null, "SKU-2", null, null,
                Set.of(CategoryId.generate()), UUID.randomUUID(), Money.of("300", "USD"), true, true, "zapatos", AT));

        SearchResults<SearchDocument> results = repository.search(new SearchQuery(null, category.value(),
                brand, new BigDecimal("100"), new BigDecimal("1000"), null, null,
                SearchQuery.SortBy.RELEVANCE, 0, 10));

        assertThat(results.total()).isEqualTo(1);
        assertThat(results.items().get(0).getName()).isEqualTo("Camisa");
    }

    @Test
    void filtraPorDisponibilidadYAtributos() {
        repository.save(SearchDocument.create(ProductId.generate(), "Camisa", null, "SKU-1", null,
                "color:blanco", Set.of(), null, Money.of("900", "USD"), true, true, "camisa color:blanco", AT));
        repository.save(SearchDocument.create(ProductId.generate(), "Saco", null, "SKU-2", null,
                "color:negro", Set.of(), null, Money.of("500", "USD"), false, true, "saco color:negro", AT));

        SearchResults<SearchDocument> disponibles = repository.search(
                new SearchQuery(null, null, null, null, null, true, null, SearchQuery.SortBy.RELEVANCE, 0, 10));
        assertThat(disponibles.total()).isEqualTo(1);
        assertThat(disponibles.items().get(0).getName()).isEqualTo("Camisa");

        SearchResults<SearchDocument> porColor = repository.search(
                new SearchQuery(null, null, null, null, null, null, java.util.Map.of("color", "negro"),
                        SearchQuery.SortBy.RELEVANCE, 0, 10));
        assertThat(porColor.total()).isEqualTo(1);
        assertThat(porColor.items().get(0).getName()).isEqualTo("Saco");
    }

    @Test
    void paginaYOrdenaResultados() {
        repository.save(documento("A", "a", "SKU-1"));
        repository.save(documento("B", "b", "SKU-2"));
        repository.save(documento("C", "c", "SKU-3"));

        SearchResults<SearchDocument> page = repository.search(
                new SearchQuery(null, null, null, null, null, null, null, SearchQuery.SortBy.NAME_ASC, 0, 2));

        assertThat(page.total()).isEqualTo(3);
        assertThat(page.items()).hasSize(2);
        assertThat(page.items().get(0).getName()).isEqualTo("A");
        assertThat(page.items().get(1).getName()).isEqualTo("B");
    }

    @Test
    void autocompleteDevuelveNombresPorPrefijo() {
        repository.save(documento("Camisa", "camisa", "SKU-1"));
        repository.save(documento("Camisa de manga larga", "camisa manga", "SKU-2"));
        repository.save(documento("Pantalón", "pantalón", "SKU-3"));

        assertThat(repository.findAutocompleteNames("cami", 5))
                .containsExactly("Camisa", "Camisa de manga larga");
    }

    @Test
    void findByIndexStatusDevuelveFallidos() {
        SearchDocument ok = documento("Camisa", "camisa", "SKU-1");
        repository.save(ok);

        SearchDocument failed = documento("Saco", "saco", "SKU-2");
        failed.markFailed("timeout", AT);
        repository.save(failed);

        assertThat(repository.findByIndexStatus(IndexStatus.FAILED)).hasSize(1);
        assertThat(repository.findByIndexStatus(IndexStatus.FAILED).get(0).getName()).isEqualTo("Saco");
    }

    private static SearchDocument documento(String name, String text, String sku) {
        return SearchDocument.create(ProductId.generate(), name, null, sku, null, null, Set.of(),
                null, Money.of("500", "USD"), true, true, text, AT);
    }
}