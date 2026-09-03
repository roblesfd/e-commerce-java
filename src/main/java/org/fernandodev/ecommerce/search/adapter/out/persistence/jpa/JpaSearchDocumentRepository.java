package org.fernandodev.ecommerce.search.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.search.adapter.out.persistence.jpa.entity.SearchDocumentJpaEntity;
import org.fernandodev.ecommerce.search.adapter.util.mapper.SearchDocumentPersistenceMapper;
import org.fernandodev.ecommerce.search.application.dto.SearchQuery;
import org.fernandodev.ecommerce.search.application.dto.SearchResults;
import org.fernandodev.ecommerce.search.application.port.out.SearchDocumentRepository;
import org.fernandodev.ecommerce.search.domain.SearchDocument;
import org.fernandodev.ecommerce.search.domain.value_object.IndexStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link SearchDocumentRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaSearchDocumentRepository implements SearchDocumentRepository {

    private final SearchDocumentSpringDataRepository springData;

    @Override
    public SearchDocument save(SearchDocument document) {
        SearchDocumentJpaEntity saved = springData.save(SearchDocumentPersistenceMapper.toEntity(document));
        return SearchDocumentPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<SearchDocument> findById(ProductId productId) {
        return springData.findById(productId.value()).map(SearchDocumentPersistenceMapper::toDomain);
    }

    @Override
    public List<SearchDocument> findByIndexStatus(IndexStatus status) {
        return springData.findByIndexStatus(status).stream()
                .map(SearchDocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(ProductId productId) {
        springData.deleteById(productId.value());
    }

    @Override
    public List<ProductId> findAllProductIds() {
        return springData.findAllProductIds().stream().map(ProductId::of).toList();
    }

    @Override
    public SearchResults<SearchDocument> search(SearchQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.size(), sort(query));
        Page<SearchDocumentJpaEntity> page = springData.findAll(SearchDocumentSpecification.of(query), pageable);
        List<SearchDocument> items = page.getContent().stream()
                .map(SearchDocumentPersistenceMapper::toDomain)
                .toList();
        return new SearchResults<>(items, page.getTotalElements(), query.page(), query.size());
    }

    @Override
    public List<String> findAutocompleteNames(String prefix, int limit) {
        if (prefix.isEmpty()) {
            return List.of();
        }
        return springData.findAutocompleteNames(prefix + "%", PageRequest.of(0, limit));
    }

    @Override
    public List<String> findSuggestions(String q, int limit) {
        if (q.isEmpty()) {
            return List.of();
        }
        return springData.findSuggestionNames("%" + q + "%", PageRequest.of(0, limit));
    }

    private Sort sort(SearchQuery query) {
        return switch (query.sort()) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price");
            case NAME_ASC -> Sort.by(Sort.Direction.ASC, "name");
            case RELEVANCE -> Sort.unsorted();
        };
    }
}