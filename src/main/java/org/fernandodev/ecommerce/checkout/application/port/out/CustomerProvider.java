package org.fernandodev.ecommerce.checkout.application.port.out;

import java.util.UUID;

/**
 * Proveedor de la dirección del cliente (contexto Customer Management):
 * valida que la dirección pertenezca al cliente y devuelve su snapshot.
 */
public interface CustomerProvider {

/**
     * Valida que la dirección pertenezca al cliente y devuelve su snapshot.
     *
     * @param customerId identificador del cliente.
     * @param addressId  identificador de la dirección.
     * @return el snapshot de la dirección.
     * @throws org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException
     *         si la dirección no existe o no pertenece al cliente.
     */
    String addressSnapshotOf(UUID customerId, UUID addressId);
}