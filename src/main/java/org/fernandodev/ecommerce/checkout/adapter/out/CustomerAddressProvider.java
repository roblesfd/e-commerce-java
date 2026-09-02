package org.fernandodev.ecommerce.checkout.adapter.out;

import java.util.UUID;

import org.fernandodev.ecommerce.checkout.application.port.out.CustomerProvider;
import org.fernandodev.ecommerce.customer.application.port.in.CustomerUseCases;
import org.fernandodev.ecommerce.customer.domain.Customer;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador que implementa {@link CustomerProvider} consultando el contexto
 * Customer Management.
 */
@Component
@RequiredArgsConstructor
public class CustomerAddressProvider implements CustomerProvider {

    private final CustomerUseCases customerUseCases;

    @Override
    public String addressSnapshotOf(UUID customerId, UUID addressId) {
        Customer customer = customerUseCases.getByIamUserId(customerId);
        return customer.getAddresses().stream()
                .filter(address -> address.id().value().equals(addressId))
                .findFirst()
                .map(address -> address.street() + ", " + address.city() + ", " + address.country())
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada: " + addressId));
    }
}