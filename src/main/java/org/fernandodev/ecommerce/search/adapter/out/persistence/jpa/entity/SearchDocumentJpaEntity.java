package org.fernandodev.ecommerce.search.adapter.out.persistence.jpa.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.search.domain.value_object.IndexStatus;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA del documento del índice de búsqueda.
 */
@Entity
@Table(name = "search_document")
@Getter
@Setter
@NoArgsConstructor
public class SearchDocumentJpaEntity {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "sku", length = 64)
    private String sku;

    @Column(name = "barcode", length = 64)
    private String barcode;

    @Column(name = "attributes")
    private String attributes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "search_document_category", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "category_id")
    private List<UUID> categoryIds = new ArrayList<>();

    @Column(name = "brand_id")
    private UUID brandId;

    @Column(name = "price", precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "available", nullable = false)
    private boolean available;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "searchable_text", nullable = false)
    private String searchableText;

    @Enumerated(EnumType.STRING)
    @Column(name = "index_status", nullable = false, length = 10)
    private IndexStatus indexStatus;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}