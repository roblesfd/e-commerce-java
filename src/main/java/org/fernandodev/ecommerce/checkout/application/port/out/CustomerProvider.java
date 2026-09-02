package org.fernandodev.ecommerce.checkout.application.port.out;

import java.util.UUID;

/**
 * Proveedor de la dirección del cliente (contexto Customer Management):
 * valida que la dirección pertenezca al cliente y devuelve su snapshot.
 */
public interface CustomerProvider {

    String addressSnapshotOf(UUID customerId, UUID addressId);
}