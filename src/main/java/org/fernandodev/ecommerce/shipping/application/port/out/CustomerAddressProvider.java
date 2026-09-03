package org.fernandodev.ecommerce.shipping.application.port.out;

import java.util.UUID;

/**
 * Proveedor de la dirección del cliente (contexto Customer Management).
 */
public interface CustomerAddressProvider {

    String addressSnapshotOf(UUID customerId, UUID addressId);
}