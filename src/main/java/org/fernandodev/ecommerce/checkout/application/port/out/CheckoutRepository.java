package org.fernandodev.ecommerce.checkout.application.port.out;

import java.util.Optional;

import org.fernandodev.ecommerce.checkout.domain.Checkout;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutId;

/**
 * Puerto de salida de persistencia del agregado {@link Checkout} (Redis).
 */
public interface CheckoutRepository {

/**
     * @param idempotencyKey clave de idempotencia.
     * @return el checkout asociado a la clave, o {@code Optional.empty()} si no existe.
     */
    Optional<Checkout> findByIdempotencyKey(String idempotencyKey);

/**
     * @param id identificador del checkout.
     * @return el checkout, o {@code Optional.empty()} si no existe.
     */
    Optional<Checkout> findById(CheckoutId id);

    /**
     * Persiste el checkout usando SETNX sobre la clave de idempotencia.
     * Devuelve el checkout ya existente si la clave estaba ocupada.
     */
    Optional<Checkout> saveIfAbsent(Checkout checkout);

/**
     * Persiste el checkout.
     *
     * @param checkout checkout a guardar.
     * @return el checkout persistido.
     */
    Checkout save(Checkout checkout);
}