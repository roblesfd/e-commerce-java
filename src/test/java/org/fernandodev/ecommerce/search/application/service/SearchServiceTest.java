package org.fernandodev.ecommerce.search.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.search.application.dto.SearchQuery;
import org.fernandodev.ecommerce.search.application.dto.SearchResult;
import org.fernandodev.ecommerce.search.application.dto.SearchResults;
import org.fernandodev.ecommerce.search.application.port.out.SearchDocumentRepository;
import org.fernandodev.ecommerce.search.domain.SearchDocument;
import org.fernandodev.ecommerce.search.domain.value_object.IndexStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;

class SearchServiceTest {

    private final FakeSearchRepository repository = new FakeSearchRepository();
    private final SearchService service = new SearchService(repository);

    @Test
    void searchDevuelveResultadosMapeados() {
        repository.docs = List.of(documento());

        SearchResults<SearchResult> results = service.search(new SearchQuery("camisa", null, null, null, null, null,
                null, SearchQuery.SortBy.RELEVANCE, 0, 20));

        assertThat(results.total()).isEqualTo(1);
        assertThat(results.items()).hasSize(1);
        assertThat(results.items().get(0).name()).isEqualTo("Camisa");
        assertThat(results.items().get(0).price()).isEqualByComparingTo(new BigDecimal("900.00"));
    }

    @Test
    void searchSinResultadosDevuelveVacio() {
        repository.docs = List.of();

        SearchResults<SearchResult> results = service.search(new SearchQuery("nada", null, null, null, null, null,
                null, SearchQuery.SortBy.RELEVANCE, 0, 20));

        assertThat(results.total()).isZero();
        assertThat(results.items()).isEmpty();
    }

    @Test
    void autocompleteDevuelveNombres() {
        repository.autocomplete = List.of("Camisa", "Camisa de manga larga");

        assertThat(service.autocomplete("cami", 5)).containsExactly("Camisa", "Camisa de manga larga");
    }

    @Test
    void suggestDevuelveNombres() {
        repository.suggestions = List.of("Camisa blanca");

        assertThat(service.suggest("blan", 5)).containsExactly("Camisa blanca");
    }

    private static SearchDocument documento() {
        return SearchDocument.create(ProductId.generate(), "Camisa", "Camisa de algodón", "SKU-1", null,
                "color:blanco", Set.of(CategoryId.generate()), UUID.randomUUID(), Money.of("900", "USD"),
                true, true, "camisa", Instant.parse("2026-01-01T00:00:00Z"));
    }

    private static final class FakeSearchRepository implements SearchDocumentRepository {

        private List<SearchDocument> docs = List.of();
        private List<String> autocomplete = List.of();
        private List<String> suggestions = List.of();

        @Override
        public SearchDocument save(SearchDocument document) {
            return document;
        }

        @Override
        public Optional<SearchDocument> findById(ProductId productId) {
            return docs.stream().filter(d -> d.getProductId().equals(productId)).findFirst();
        }

        @Override
        public List<SearchDocument> findByIndexStatus(IndexStatus status) {
            return docs.stream().filter(d -> d.getIndexStatus() == status).toList();
        }

        @Override
        public void delete(ProductId productId) {
            docs = docs.stream().filter(d -> !d.getProductId().equals(productId)).toList();
        }

        @Override
        public List<ProductId> findAllProductIds() {
            return docs.stream().map(SearchDocument::getProductId).toList();
        }

        @Override
        public SearchResults<SearchDocument> search(SearchQuery query) {
            return new SearchResults<>(docs, docs.size(), query.page(), query.size());
        }

        @Override
        public List<String> findAutocompleteNames(String prefix, int limit) {
            return autocomplete;
        }

        @Override
        public List<String> findSuggestions(String q, int limit) {
            return suggestions;
        }
    }
}