package org.fernandodev.ecommerce.checkout.application.port.in;

import java.util.UUID;

import org.fernandodev.ecommerce.checkout.application.dto.StartCheckoutCommand;
import org.fernandodev.ecommerce.checkout.domain.Checkout;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutId;

/**
 * Casos de uso del contexto Checkout (puerto de entrada).
 */
public interface CheckoutUseCases {

    Checkout start(UUID customerId, String idempotencyKey, StartCheckoutCommand command);

    Checkout getById(UUID customerId, CheckoutId id);

    Checkout confirm(UUID customerId, CheckoutId id);

    Checkout cancel(UUID customerId, CheckoutId id);
}