package org.fernandodev.ecommerce.catalog.application.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Datos de creación o actualización de un producto.
 * <p>
 * La actualización se trata como reemplazo completo (semántica PUT): las
 * variantes, atributos, imágenes y categorías se sustituyen por completo.
 * </p>
 *
 * @param sku          código de unidad de existencias (obligatorio).
 * @param barcode      código de barras (opcional).
 * @param name         nombre del producto (obligatorio).
 * @param description  descripción (opcional).
 * @param brandId      identificador de la marca (opcional).
 * @param categoryIds  identificadores de categorías asociadas.
 * @param variants     variantes del producto.
 * @param attributes   atributos del producto.
 * @param images       URLs de las imágenes del producto.
 */
public record ProductCommand(
        String sku,
        String barcode,
        String name,
        String description,
        UUID brandId,
        Set<UUID> categoryIds,
        List<VariantCommand> variants,
        List<AttributeCommand> attributes,
        List<String> images
) {

    /**
     * Constructor compacto que normaliza las colecciones opcionales a listas vacías.
     */
    public ProductCommand {
        categoryIds = categoryIds == null ? Set.of() : categoryIds;
        variants = variants == null ? List.of() : variants;
        attributes = attributes == null ? List.of() : attributes;
        images = images == null ? List.of() : images;
    }

    /**
     * Variante de producto (su SKU y barcode son opcionales).
     *
     * @param sku        SKU de la variante (opcional).
     * @param barcode    barcode de la variante (opcional).
     * @param attributes atributos de la variante.
     */
    public record VariantCommand(String sku, String barcode, List<AttributeCommand> attributes) {

        /**
         * Constructor compacto que normaliza los atributos a lista vacía.
         */
        public VariantCommand {
            attributes = attributes == null ? List.of() : attributes;
        }
    }

    /**
     * Atributo de producto o variante.
     *
     * @param name  nombre del atributo.
     * @param value valor del atributo.
     */
    public record AttributeCommand(String name, String value) {
    }
}