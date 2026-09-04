package org.fernandodev.ecommerce.search.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.search.domain.value_object.IndexStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Documento del índice de búsqueda (read-model denormalizado).
 * <p>
 * No es la fuente de verdad del producto: el catálogo lo es. Si la indexación
 * falla, el documento queda en {@link IndexStatus#FAILED} con el error y el
 * número de intentos para su reintento.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchDocument {

    private ProductId productId;
    private String name;
    private String description;
    private String sku;
    private String barcode;
    private String attributes;
    @Getter(AccessLevel.NONE)
    private Set<CategoryId> categoryIds;
    private UUID brandId;
    private Money price;
    private boolean available;
    private boolean active;
    private String searchableText;
    private IndexStatus indexStatus;
    private String lastError;
    private int attempts;
    private Instant updatedAt;

    /**
     * Factoría del agregado.
     */
    public static SearchDocument create(
            ProductId productId,
            String name,
            String description,
            String sku,
            String barcode,
            String attributes,
            Set<CategoryId> categoryIds,
            UUID brandId,
            Money price,
            boolean available,
            boolean active,
            String searchableText,
            Instant at
    ) {
        SearchDocument document = new SearchDocument();
        document.productId = Objects.requireNonNull(productId, "productId no puede ser null");
        document.name = Objects.requireNonNull(name, "name no puede ser null");
        document.description = description;
        document.sku = sku;
        document.barcode = barcode;
        document.attributes = attributes;
        document.categoryIds = new HashSet<>(categoryIds == null ? Set.of() : categoryIds);
        document.brandId = brandId;
        document.price = price;
        document.available = available;
        document.active = active;
        document.searchableText = Objects.requireNonNull(searchableText, "searchableText no puede ser null");
        document.indexStatus = IndexStatus.SYNCED;
        document.lastError = null;
        document.attempts = 0;
        document.updatedAt = Objects.requireNonNull(at, "at no puede ser null");
        return document;
    }

    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     */
    /**
     * Factoría completa usada por la persistencia.
     */
    public static SearchDocument crear(
            ProductId productId,
            String name,
            String description,
            String sku,
            String barcode,
            String attributes,
            Set<CategoryId> categoryIds,
            UUID brandId,
            Money price,
            boolean available,
            boolean active,
            String searchableText,
            IndexStatus indexStatus,
            String lastError,
            int attempts,
            Instant updatedAt
    ) {
        SearchDocument document = new SearchDocument();
        document.productId = Objects.requireNonNull(productId, "productId no puede ser null");
        document.name = Objects.requireNonNull(name, "name no puede ser null");
        document.description = description;
        document.sku = sku;
        document.barcode = barcode;
        document.attributes = attributes;
        document.categoryIds = new HashSet<>(categoryIds == null ? Set.of() : categoryIds);
        document.brandId = brandId;
        document.price = price;
        document.available = available;
        document.active = active;
        document.searchableText = Objects.requireNonNull(searchableText, "searchableText no puede ser null");
        document.indexStatus = Objects.requireNonNull(indexStatus, "indexStatus no puede ser null");
        document.lastError = lastError;
        document.attempts = attempts;
        document.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt no puede ser null");
        return document;
    }

    public Set<CategoryId> getCategoryIds() {
        return Collections.unmodifiableSet(categoryIds);
    }

    public void markSynced(Instant at) {
        this.indexStatus = IndexStatus.SYNCED;
        this.lastError = null;
        this.updatedAt = at;
    }

    public void markFailed(String error, Instant at) {
        this.indexStatus = IndexStatus.FAILED;
        this.lastError = error;
        this.attempts++;
        this.updatedAt = at;
    }
}