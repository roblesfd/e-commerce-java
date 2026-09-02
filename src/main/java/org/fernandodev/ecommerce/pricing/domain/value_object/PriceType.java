package org.fernandodev.ecommerce.pricing.domain.value_object;

/**
 * Tipo de precio que se aplica por encima del precio base.
 */
public enum PriceType {

    /** Descuento temporal: debe ser menor que el precio base. */
    PROMOTIONAL,

    /** Precio programado en una ventana de tiempo (puede ser mayor o menor). */
    TIME_BASED
}