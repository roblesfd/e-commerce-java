package org.fernandodev.ecommerce.catalog.adapter.in.rest.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición para crear o actualizar un producto (semántica PUT).
 */
public record ProductRequest(
        @NotBlank @Size(max = 64) String sku,
        @Size(max = 64) String barcode,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        UUID brandId,
        Set<UUID> categoryIds,
        List<@Valid VariantRequest> variants,
        List<@Valid AttributeRequest> attributes,
        List<@Size(max = 500) String> images
) {

    /**
     * Constructor compacto que normaliza las colecciones opcionales a listas vacías.
     */
    public ProductRequest {
        categoryIds = categoryIds == null ? Set.of() : categoryIds;
        variants = variants == null ? List.of() : variants;
        attributes = attributes == null ? List.of() : attributes;
        images = images == null ? List.of() : images;
    }

    /**
     * Variante de producto en la petición (SKU y barcode opcionales).
     *
     * @param sku        SKU de la variante (opcional).
     * @param barcode    barcode de la variante (opcional).
     * @param attributes atributos de la variante.
     */
    public record VariantRequest(
            @Size(max = 64) String sku,
            @Size(max = 64) String barcode,
            List<@Valid AttributeRequest> attributes
    ) {
        /**
         * Constructor compacto que normaliza los atributos a lista vacía.
         */
        public VariantRequest {
            attributes = attributes == null ? List.of() : attributes;
        }
    }

    /**
     * Atributo de producto o variante en la petición.
     *
     * @param name  nombre del atributo.
     * @param value valor del atributo.
     */
    public record AttributeRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 500) String value
    ) {
    }
}