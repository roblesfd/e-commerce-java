package org.fernandodev.ecommerce.customer.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.customer.adapter.in.rest.dto.AddressRequest;
import org.fernandodev.ecommerce.customer.adapter.in.rest.dto.AddressResponse;
import org.fernandodev.ecommerce.customer.adapter.in.rest.dto.CreateCustomerRequest;
import org.fernandodev.ecommerce.customer.adapter.in.rest.dto.CustomerResponse;
import org.fernandodev.ecommerce.customer.adapter.in.rest.dto.PreferencesRequest;
import org.fernandodev.ecommerce.customer.adapter.in.rest.dto.UpdateProfileRequest;
import org.fernandodev.ecommerce.customer.adapter.in.rest.mapper.CustomerMapper;
import org.fernandodev.ecommerce.customer.application.CustomerUseCases;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Controlador REST del contexto Customer Management.
 * <p>
 * Todos los recursos se acceden mediante {@code /me}, resolviendo el cliente a
 * partir del usuario IAM autenticado (propiedad por token).
 * </p>
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerUseCases customerUseCases;
    private final CustomerMapper mapper;

    public CustomerController(CustomerUseCases customerUseCases, CustomerMapper mapper) {
        this.customerUseCases = customerUseCases;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse register(@Valid @RequestBody CreateCustomerRequest request) {
        return mapper.toResponse(
                customerUseCases.register(SecurityUtils.requiredUserId(), mapper.toProfile(request)));
    }

    @GetMapping("/me")
    public CustomerResponse me() {
        return mapper.toResponse(customerUseCases.getByIamUserId(SecurityUtils.requiredUserId()));
    }

    @PatchMapping("/me")
    public CustomerResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return mapper.toResponse(
                customerUseCases.updateProfile(SecurityUtils.requiredUserId(), mapper.toProfile(request)));
    }

    @GetMapping("/me/addresses")
    public List<AddressResponse> addresses() {
        return mapper.toAddressesResponse(customerUseCases.listAddresses(SecurityUtils.requiredUserId()));
    }

    @PostMapping("/me/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse addAddress(@Valid @RequestBody AddressRequest request) {
        return mapper.toResponse(
                customerUseCases.addAddress(SecurityUtils.requiredUserId(), mapper.toAddressCommand(request)));
    }

    @PutMapping("/me/addresses/{addressId}")
    public CustomerResponse updateAddress(@PathVariable UUID addressId, @Valid @RequestBody AddressRequest request) {
        return mapper.toResponse(customerUseCases.updateAddress(
                SecurityUtils.requiredUserId(), addressId, mapper.toAddressCommand(request)));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAddress(@PathVariable UUID addressId) {
        customerUseCases.removeAddress(SecurityUtils.requiredUserId(), addressId);
    }

    @PatchMapping("/me/addresses/{addressId}/default")
    public CustomerResponse setDefaultAddress(@PathVariable UUID addressId) {
        return mapper.toResponse(customerUseCases.setDefaultAddress(SecurityUtils.requiredUserId(), addressId));
    }

    @PatchMapping("/me/preferences")
    public CustomerResponse updatePreferences(@Valid @RequestBody PreferencesRequest request) {
        return mapper.toResponse(customerUseCases.updatePreferences(
                SecurityUtils.requiredUserId(), mapper.toPreferencesCommand(request)));
    }
}