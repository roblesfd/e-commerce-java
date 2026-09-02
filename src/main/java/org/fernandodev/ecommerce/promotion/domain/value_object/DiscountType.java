package org.fernandodev.ecommerce.promotion.domain.value_object;

/**
 * Tipo de descuento de una promoción.
 */
public enum DiscountType {

    /** Porcentaje sobre el subtotal elegible. */
    PERCENTAGE,

    /** Monto fijo de descuento. */
    FIXED_AMOUNT,

    /** Descuento por volumen según tramos de cantidad mínima. */
    VOLUME,

    /** Compra N y recibe M gratis (por línea de producto). */
    BUY_X_GET_Y
}