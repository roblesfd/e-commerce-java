package org.fernandodev.ecommerce.checkout.domain.value_object;

/**
 * Estado del proceso de checkout.
 */
public enum CheckoutStatus {

    /** Iniciado: carrito validado e inventario reservado. */
    PENDING,

    /** Orden creada. */
    ORDER_CREATED,

    /** Pago iniciado. */
    PAYMENT_PENDING,

    /** Checkout completado: pago aprobado y orden confirmada. */
    CONFIRMED,

    /** Falló el pago: orden cancelada y reservas liberadas. */
    FAILED,

    /** Cancelado por el cliente: reservas liberadas. */
    CANCELLED
}