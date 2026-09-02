package org.fernandodev.ecommerce.customer.application.service;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.customer.application.dto.AddressCommand;
import org.fernandodev.ecommerce.customer.application.dto.PreferencesCommand;
import org.fernandodev.ecommerce.customer.application.dto.Profile;
import org.fernandodev.ecommerce.customer.application.port.in.CustomerUseCases;
import org.fernandodev.ecommerce.customer.application.port.out.CustomerRepository;
import org.fernandodev.ecommerce.customer.domain.value_object.Address;
import org.fernandodev.ecommerce.customer.domain.value_object.AddressId;
import org.fernandodev.ecommerce.customer.domain.Customer;
import org.fernandodev.ecommerce.customer.domain.value_object.CustomerId;
import org.fernandodev.ecommerce.customer.domain.value_object.CustomerPreferences;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.fernandodev.model.value_object.Email;
import org.fernandodev.model.value_object.PhoneNumber;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación del contexto Customer Management.
 * <p>
 * Orquesta los casos de uso y aplica reglas de aplicación (p. ej. unicidad de
 * email) sobre el puerto de persistencia. No depende de Spring.
 * </p>
 */
@RequiredArgsConstructor
public class CustomerService implements CustomerUseCases {

    private final CustomerRepository repository;

    @Override
    public Customer register(UUID iamUserId, Profile profile) {
        String email = profile.email().trim();
        if (repository.existsByEmail(email)) {
            throw new ConflictException("Ya existe un cliente con el email: " + email);
        }
        Customer customer = Customer.register(
                CustomerId.generate(),
                iamUserId,
                profile.firstName(),
                profile.lastName(),
                new Email(profile.email()),
                profile.phoneNumber() == null ? null : new PhoneNumber(profile.phoneNumber()));
        return repository.save(customer);
    }

    @Override
    public Customer getByIamUserId(UUID iamUserId) {
        return repository.findByIamUserId(iamUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para el usuario: " + iamUserId));
    }

    @Override
    public Customer updateProfile(UUID iamUserId, Profile profile) {
        Customer customer = getByIamUserId(iamUserId);
        String email = profile.email().trim();
        if (!customer.getEmail().direccion().equals(email) && repository.existsByEmail(email)) {
            throw new ConflictException("Ya existe un cliente con el email: " + email);
        }
        customer.updateProfile(
                profile.firstName(),
                profile.lastName(),
                new Email(email),
                profile.phoneNumber() == null ? null : new PhoneNumber(profile.phoneNumber()));
        return repository.save(customer);
    }

    @Override
    public Customer addAddress(UUID iamUserId, AddressCommand address) {
        Customer customer = getByIamUserId(iamUserId);
        customer.addAddress(address.street(), address.city(), address.state(), address.postalCode(), address.country());
        return repository.save(customer);
    }

    @Override
    public Customer updateAddress(UUID iamUserId, UUID addressId, AddressCommand address) {
        Customer customer = getByIamUserId(iamUserId);
        customer.updateAddress(AddressId.of(addressId), address.street(), address.city(), address.state(),
                address.postalCode(), address.country());
        return repository.save(customer);
    }

    @Override
    public Customer removeAddress(UUID iamUserId, UUID addressId) {
        Customer customer = getByIamUserId(iamUserId);
        customer.removeAddress(AddressId.of(addressId));
        return repository.save(customer);
    }

    @Override
    public Customer setDefaultAddress(UUID iamUserId, UUID addressId) {
        Customer customer = getByIamUserId(iamUserId);
        customer.setDefaultAddress(AddressId.of(addressId));
        return repository.save(customer);
    }

    @Override
    public Customer updatePreferences(UUID iamUserId, PreferencesCommand preferences) {
        Customer customer = getByIamUserId(iamUserId);
        customer.updatePreferences(new CustomerPreferences(preferences.newsletterEnabled(), preferences.preferredCurrencyCode()));
        return repository.save(customer);
    }

    @Override
    public List<Address> listAddresses(UUID iamUserId) {
        return getByIamUserId(iamUserId).getAddresses();
    }
}
