package org.fernandodev.ecommerce.catalog.adapter.in.rest.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Respuesta con el detalle completo de un producto.
 */
public record ProductResponse(
        UUID id,
        String sku,
        String barcode,
        String name,
        String description,
        boolean active,
        UUID brandId,
        Set<UUID> categoryIds,
        List<VariantResponse> variants,
        List<AttributeResponse> attributes,
        List<String> images
) {

    /**
     * Variante del producto en la respuesta.
     *
     * @param id         identificador de la variante.
     * @param sku        SKU de la variante (opcional).
     * @param barcode    barcode de la variante (opcional).
     * @param attributes atributos de la variante.
     */
    public record VariantResponse(UUID id, String sku, String barcode, List<AttributeResponse> attributes) {
    }

    /**
     * Atributo del producto o variante en la respuesta.
     *
     * @param name  nombre del atributo.
     * @param value valor del atributo.
     */
    public record AttributeResponse(String name, String value) {
    }
}