package org.fernandodev.ecommerce.search.adapter.util.mapper;

import java.util.List;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.search.adapter.out.persistence.jpa.entity.SearchDocumentJpaEntity;
import org.fernandodev.ecommerce.search.domain.SearchDocument;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Mapeo entre el agregado {@link SearchDocument} y su entidad JPA.
 */
public final class SearchDocumentPersistenceMapper {

    private SearchDocumentPersistenceMapper() {
    }

    public static SearchDocumentJpaEntity toEntity(SearchDocument document) {
        SearchDocumentJpaEntity entity = new SearchDocumentJpaEntity();
        entity.setProductId(document.getProductId().value());
        entity.setName(document.getName());
        entity.setDescription(document.getDescription());
        entity.setSku(document.getSku());
        entity.setBarcode(document.getBarcode());
        entity.setAttributes(document.getAttributes());
        entity.setCategoryIds(document.getCategoryIds().stream().map(CategoryId::value).toList());
        entity.setBrandId(document.getBrandId());
        entity.setPrice(document.getPrice() == null ? null : document.getPrice().amount());
        entity.setCurrency(document.getPrice() == null ? null : document.getPrice().currencyCode());
        entity.setAvailable(document.isAvailable());
        entity.setActive(document.isActive());
        entity.setSearchableText(document.getSearchableText());
        entity.setIndexStatus(document.getIndexStatus());
        entity.setLastError(document.getLastError());
        entity.setAttempts(document.getAttempts());
        return entity;
    }

    public static SearchDocument toDomain(SearchDocumentJpaEntity entity) {
        Money price = entity.getPrice() == null || entity.getCurrency() == null
                ? null
                : Money.of(entity.getPrice(), entity.getCurrency());
        List<CategoryId> categoryIds = entity.getCategoryIds().stream().map(CategoryId::of).toList();
        return SearchDocument.crear(
                ProductId.of(entity.getProductId()),
                entity.getName(),
                entity.getDescription(),
                entity.getSku(),
                entity.getBarcode(),
                entity.getAttributes(),
                java.util.Set.copyOf(categoryIds),
                entity.getBrandId(),
                price,
                entity.isAvailable(),
                entity.isActive(),
                entity.getSearchableText(),
                entity.getIndexStatus(),
                entity.getLastError(),
                entity.getAttempts(),
                entity.getUpdatedAt() == null ? java.time.Instant.now() : entity.getUpdatedAt());
    }
}