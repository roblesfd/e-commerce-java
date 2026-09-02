package org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa.entity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA de una variante de producto.
 */
@Entity
@Table(name = "product_variant")
@Getter
@Setter
@NoArgsConstructor
public class ProductVariantJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductJpaEntity product;

    @Column(name = "sku", unique = true)
    private String sku;

    @Column(name = "barcode", unique = true)
    private String barcode;

    @Column(name = "position", nullable = false)
    private int position;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_variant_attribute", joinColumns = @JoinColumn(name = "variant_id"))
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    private Map<String, String> attributes = new LinkedHashMap<>();
}
