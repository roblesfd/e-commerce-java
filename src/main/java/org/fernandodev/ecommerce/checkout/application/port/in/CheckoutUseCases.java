package org.fernandodev.ecommerce.checkout.application.port.in;

import java.util.UUID;

import org.fernandodev.ecommerce.checkout.application.dto.StartCheckoutCommand;
import org.fernandodev.ecommerce.checkout.domain.Checkout;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutId;

/**
 * Casos de uso del contexto Checkout (puerto de entrada).
 */
public interface CheckoutUseCases {

    /**
     * Inicia un checkout: valida carrito, reserva inventario y calcula el
     * descuento del cupón. Idempotente por {@code idempotencyKey}.
     *
     * @param customerId     identificador del cliente.
     * @param idempotencyKey clave de idempotencia.
     * @param command        datos del checkout.
     * @return el checkout en estado {@code PENDING}.
     */
    Checkout start(UUID customerId, String idempotencyKey, StartCheckoutCommand command);

    /**
     * @param customerId identificador del cliente (propiedad).
     * @param id         identificador del checkout.
     * @return el checkout del cliente.
     * @throws org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException
     *         si no existe o no pertenece al cliente.
     */
    Checkout getById(UUID customerId, CheckoutId id);

    /**
     * Confirma el checkout: crea la orden, inicia el pago y, según la
     * aprobación, confirma la orden o la cancela y libera las reservas.
     *
     * @param customerId identificador del cliente (propiedad).
     * @param id         identificador del checkout.
     * @return el checkout en estado final.
     */
    Checkout confirm(UUID customerId, CheckoutId id);

    /**
     * Cancela el checkout en {@code PENDING}.
     *
     * @param customerId identificador del cliente (propiedad).
     * @param id         identificador del checkout.
     * @return el checkout cancelado.
     */
    Checkout cancel(UUID customerId, CheckoutId id);
}