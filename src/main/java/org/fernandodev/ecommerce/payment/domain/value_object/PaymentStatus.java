package org.fernandodev.ecommerce.payment.domain.value_object;

/**
 * Ciclo de vida de un pago.
 * <p>
 * Transiciones: {@code PENDING→AUTHORIZED}, {@code PENDING→FAILED},
 * {@code AUTHORIZED→CAPTURED}, {@code PENDING/AUTHORIZED→CANCELLED},
 * {@code CAPTURED→REFUNDED}. Los estados terminales no se re-transicionan
 * (captura/refund idempotentes).
 * </p>
 */
public enum PaymentStatus {

    /** Pago creado, pendiente de autorización. */
    PENDING,

    /** Autorizado por el proveedor, listo para capturar. */
    AUTHORIZED,

    /** Capturado (dinero retenido). */
    CAPTURED,

    /** Rechazado o fallido por el proveedor. */
    FAILED,

    /** Cancelado. */
    CANCELLED,

    /** Reembolsado por completo. */
    REFUNDED
}