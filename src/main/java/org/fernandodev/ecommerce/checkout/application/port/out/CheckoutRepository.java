package org.fernandodev.ecommerce.checkout.application.port.out;

import java.util.Optional;

import org.fernandodev.ecommerce.checkout.domain.Checkout;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutId;

/**
 * Puerto de salida de persistencia del agregado {@link Checkout} (Redis).
 */
public interface CheckoutRepository {

    Optional<Checkout> findByIdempotencyKey(String idempotencyKey);

    Optional<Checkout> findById(CheckoutId id);

    /**
     * Persiste el checkout usando SETNX sobre la clave de idempotencia.
     * Devuelve el checkout ya existente si la clave estaba ocupada.
     */
    Optional<Checkout> saveIfAbsent(Checkout checkout);

    Checkout save(Checkout checkout);
}