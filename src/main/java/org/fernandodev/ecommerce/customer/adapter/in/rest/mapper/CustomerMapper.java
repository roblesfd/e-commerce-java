package org.fernandodev.ecommerce.customer.adapter.in.rest.mapper;

import java.util.List;

import org.fernandodev.ecommerce.customer.adapter.in.rest.dto.AddressRequest;
import org.fernandodev.ecommerce.customer.adapter.in.rest.dto.AddressResponse;
import org.fernandodev.ecommerce.customer.adapter.in.rest.dto.CreateCustomerRequest;
import org.fernandodev.ecommerce.customer.adapter.in.rest.dto.CustomerResponse;
import org.fernandodev.ecommerce.customer.adapter.in.rest.dto.PreferencesRequest;
import org.fernandodev.ecommerce.customer.adapter.in.rest.dto.UpdateProfileRequest;
import org.fernandodev.ecommerce.customer.application.dto.AddressCommand;
import org.fernandodev.ecommerce.customer.application.dto.PreferencesCommand;
import org.fernandodev.ecommerce.customer.application.dto.Profile;
import org.fernandodev.ecommerce.customer.domain.value_object.Address;
import org.fernandodev.ecommerce.customer.domain.Customer;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre DTOs REST y objetos de aplicación/dominio del contexto
 * Customer. Sin lógica de negocio.
 */
@Component
public class CustomerMapper {

    public Profile toProfile(CreateCustomerRequest request) {
        return new Profile(request.firstName(), request.lastName(), request.email(), request.phoneNumber());
    }

    public Profile toProfile(UpdateProfileRequest request) {
        return new Profile(request.firstName(), request.lastName(), request.email(), request.phoneNumber());
    }

    public AddressCommand toAddressCommand(AddressRequest request) {
        return new AddressCommand(request.street(), request.city(), request.state(), request.postalCode(), request.country());
    }

    public PreferencesCommand toPreferencesCommand(PreferencesRequest request) {
        return new PreferencesCommand(request.newsletterEnabled(), request.preferredCurrencyCode());
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId().value(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail().direccion(),
                customer.getPhoneNumber() == null ? null : customer.getPhoneNumber().value(),
                customer.getAddresses().stream().map(this::toAddressResponse).toList(),
                customer.getDefaultAddressId().map(id -> id.value()).orElse(null),
                new CustomerResponse.PreferencesResponse(
                        customer.getPreferences().newsletterEnabled(),
                        customer.getPreferences().preferredCurrencyCode()));
    }

    public AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.id().value(),
                address.street(),
                address.city(),
                address.state(),
                address.postalCode(),
                address.country());
    }

    public List<AddressResponse> toAddressesResponse(List<Address> addresses) {
        return addresses.stream().map(this::toAddressResponse).toList();
    }
}
