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

    public record VariantResponse(UUID id, String sku, String barcode, List<AttributeResponse> attributes) {
    }

    public record AttributeResponse(String name, String value) {
    }
}