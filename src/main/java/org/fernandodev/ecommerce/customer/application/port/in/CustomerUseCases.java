package org.fernandodev.ecommerce.customer.application.port.in;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.customer.application.dto.AddressCommand;
import org.fernandodev.ecommerce.customer.application.dto.PreferencesCommand;
import org.fernandodev.ecommerce.customer.application.dto.Profile;
import org.fernandodev.ecommerce.customer.domain.value_object.Address;
import org.fernandodev.ecommerce.customer.domain.Customer;

/**
 * Casos de uso del contexto Customer Management (puerto de entrada).
 */
public interface CustomerUseCases {

    Customer register(UUID iamUserId, Profile profile);

    Customer getByIamUserId(UUID iamUserId);

    Customer updateProfile(UUID iamUserId, Profile profile);

    Customer addAddress(UUID iamUserId, AddressCommand address);

    Customer updateAddress(UUID iamUserId, UUID addressId, AddressCommand address);

    Customer removeAddress(UUID iamUserId, UUID addressId);

    Customer setDefaultAddress(UUID iamUserId, UUID addressId);

    Customer updatePreferences(UUID iamUserId, PreferencesCommand preferences);

    List<Address> listAddresses(UUID iamUserId);

    List<Customer> listAll();
}
