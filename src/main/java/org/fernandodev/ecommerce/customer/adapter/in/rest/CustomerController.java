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
import org.fernandodev.ecommerce.customer.application.port.in.CustomerUseCases;
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
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST del contexto Customer Management.
 * <p>
 * Todos los recursos se acceden mediante {@code /me}, resolviendo el cliente a
 * partir del usuario IAM autenticado (propiedad por token).
 * </p>
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerUseCases customerUseCases;
    private final CustomerMapper mapper;

    /**
     * Registra el perfil comercial del cliente, vinculándolo al usuario IAM del
     * token autenticado.
     *
     * @param request datos del perfil.
     * @return el cliente creado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse register(@Valid @RequestBody CreateCustomerRequest request) {
        return mapper.toResponse(
                customerUseCases.register(SecurityUtils.requiredUserId(), mapper.toProfile(request)));
    }

    /**
     * @return el perfil del cliente autenticado.
     */
    @GetMapping("/me")
    public CustomerResponse me() {
        return mapper.toResponse(customerUseCases.getByIamUserId(SecurityUtils.requiredUserId()));
    }

    /**
     * Actualiza el perfil del cliente autenticado.
     *
     * @param request nuevos datos del perfil.
     * @return el cliente actualizado.
     */
    @PatchMapping("/me")
    public CustomerResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return mapper.toResponse(
                customerUseCases.updateProfile(SecurityUtils.requiredUserId(), mapper.toProfile(request)));
    }

    /**
     * @return las direcciones del cliente autenticado.
     */
    @GetMapping("/me/addresses")
    public List<AddressResponse> addresses() {
        return mapper.toAddressesResponse(customerUseCases.listAddresses(SecurityUtils.requiredUserId()));
    }

    /**
     * Añade una dirección al cliente autenticado.
     *
     * @param request datos de la dirección.
     * @return el cliente actualizado.
     */
    @PostMapping("/me/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse addAddress(@Valid @RequestBody AddressRequest request) {
        return mapper.toResponse(
                customerUseCases.addAddress(SecurityUtils.requiredUserId(), mapper.toAddressCommand(request)));
    }

    /**
     * Actualiza una dirección del cliente autenticado.
     *
     * @param addressId identificador de la dirección.
     * @param request   nuevos datos de la dirección.
     * @return el cliente actualizado.
     */
    @PutMapping("/me/addresses/{addressId}")
    public CustomerResponse updateAddress(@PathVariable UUID addressId, @Valid @RequestBody AddressRequest request) {
        return mapper.toResponse(customerUseCases.updateAddress(
                SecurityUtils.requiredUserId(), addressId, mapper.toAddressCommand(request)));
    }

    /**
     * Elimina una dirección del cliente autenticado.
     *
     * @param addressId identificador de la dirección.
     */
    @DeleteMapping("/me/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAddress(@PathVariable UUID addressId) {
        customerUseCases.removeAddress(SecurityUtils.requiredUserId(), addressId);
    }

    /**
     * Establece la dirección predeterminada del cliente autenticado.
     *
     * @param addressId identificador de la dirección.
     * @return el cliente actualizado.
     */
    @PatchMapping("/me/addresses/{addressId}/default")
    public CustomerResponse setDefaultAddress(@PathVariable UUID addressId) {
        return mapper.toResponse(customerUseCases.setDefaultAddress(SecurityUtils.requiredUserId(), addressId));
    }

    /**
     * Actualiza las preferencias del cliente autenticado.
     *
     * @param request nuevas preferencias.
     * @return el cliente actualizado.
     */
    @PatchMapping("/me/preferences")
    public CustomerResponse updatePreferences(@Valid @RequestBody PreferencesRequest request) {
        return mapper.toResponse(customerUseCases.updatePreferences(
                SecurityUtils.requiredUserId(), mapper.toPreferencesCommand(request)));
    }
}
