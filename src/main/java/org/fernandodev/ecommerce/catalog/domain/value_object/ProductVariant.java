package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.List;
import java.util.Objects;

/**
 * Variante de un producto (p. ej. Talla/Color).
 * <p>
 * Value object inmutable hijo del agregado {@code Product}. Puede tener su propio
 * SKU, barcode y atributos. El precio y el inventario pertenecen a los bounded
 * contexts Pricing e Inventory, por lo que NO forman parte de la variante.
 * </p>
 *
 * @param id         identificador de la variante.
 * @param sku        SKU propio (opcional).
 * @param barcode    código de barras propio (opcional).
 * @param attributes atributos de la variante.
 */
public record ProductVariant(
        ProductVariantId id,
        Sku sku,
        Barcode barcode,
        List<ProductAttribute> attributes
) {

    /**
     * Constructor compacto que valida el identificador y copia los atributos.
     *
     * @throws NullPointerException si {@code id} es {@code null}.
     */
    public ProductVariant {
        Objects.requireNonNull(id, "id no puede ser null");
        attributes = attributes == null ? List.of() : List.copyOf(attributes);
    }
}