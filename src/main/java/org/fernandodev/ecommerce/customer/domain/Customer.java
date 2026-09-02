package org.fernandodev.ecommerce.customer.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import lombok.*;
import org.fernandodev.ecommerce.customer.domain.value_object.Address;
import org.fernandodev.ecommerce.customer.domain.value_object.AddressId;
import org.fernandodev.ecommerce.customer.domain.value_object.CustomerId;
import org.fernandodev.ecommerce.customer.domain.value_object.CustomerPreferences;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.fernandodev.model.value_object.Email;
import org.fernandodev.model.value_object.PhoneNumber;

/**
 * Agregado raíz del contexto Customer Management.
 * <p>
 * Representa la información comercial del cliente. La identidad y credenciales
 * pertenecen al IAM; aquí se guarda la referencia ({@code iamUserId}) y la
 * información específica de comercio (perfil, direcciones, preferencias).
 * </p>
 */
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PUBLIC)
@ToString()
public class Customer {

    private CustomerId id;
    private UUID iamUserId;
    private String firstName;
    private String lastName;
    private Email email;
    private PhoneNumber phoneNumber;
    @Getter(AccessLevel.NONE)
    private List<Address> addresses;
    @Getter(AccessLevel.NONE)
    private AddressId defaultAddressId;
    private CustomerPreferences preferences;

    public static Customer crear(
            CustomerId id,
            UUID iamUserId,
            String firstName,
            String lastName,
            Email email,
            PhoneNumber phoneNumber,
            List<Address> addresses,
            AddressId defaultAddressId,
            CustomerPreferences preferences
    ) {

        Customer c =  new Customer();

        c.id = Objects.requireNonNull(id, "id no puede ser null");
        c.iamUserId = Objects.requireNonNull(iamUserId, "iamUserId no puede ser null");
        c.firstName = requireName(firstName, "nombre");
        c.lastName = requireName(lastName, "apellido");
        c.email = Objects.requireNonNull(email, "email no puede ser null");
        c.phoneNumber = phoneNumber;
        c.addresses = new ArrayList<>(addresses == null ? List.of() : addresses);
        c.defaultAddressId = defaultAddressId;
        c.preferences = Objects.requireNonNull(preferences, "preferences no puede ser null");

        return c;
    }

    /**
     * Registra un nuevo cliente vinculado a un usuario IAM.
     */
    public static Customer register(
            CustomerId id,
            UUID iamUserId,
            String firstName,
            String lastName,
            Email email,
            PhoneNumber phoneNumber
    ) {
        return new Customer(id, iamUserId, requireName(firstName, "nombre"), requireName(lastName, "apellido"),
                email, phoneNumber, new ArrayList<>(), null, CustomerPreferences.defaults());
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public Optional<AddressId> getDefaultAddressId() {
        return Optional.ofNullable(defaultAddressId);
    }

    public void updateProfile(String firstName, String lastName, Email email, PhoneNumber phoneNumber) {
        this.firstName = requireName(firstName, "nombre");
        this.lastName = requireName(lastName, "apellido");
        this.email = Objects.requireNonNull(email, "correo no puede ser null");
        this.phoneNumber = phoneNumber;
    }

    public void updatePreferences(CustomerPreferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferencias no puede ser null");
    }

    /**
     * Añade una dirección. La primera dirección pasa a ser la predeterminada.
     *
     * @return la dirección creada.
     */
    public Address addAddress(String street, String city, String state, String postalCode, String country) {
        Address address = new Address(AddressId.generate(), street, city, state, postalCode, country);
        addresses.add(address);
        if (defaultAddressId == null) {
            defaultAddressId = address.id();
        }
        return address;
    }

    public void updateAddress(AddressId addressId, String street, String city, String state, String postalCode, String country) {
        int index = indexOf(addressId);
        addresses.set(index, new Address(addressId, street, city, state, postalCode, country));
    }

    public void removeAddress(AddressId addressId) {
        int index = indexOf(addressId);
        addresses.remove(index);
        if (addressId.equals(defaultAddressId)) {
            defaultAddressId = addresses.isEmpty() ? null : addresses.get(0).id();
        }
    }

    public void setDefaultAddress(AddressId addressId) {
        indexOf(addressId);
        defaultAddressId = addressId;
    }

    private int indexOf(AddressId addressId) {

        return addresses.stream()
                .filter(a -> a.id().equals(addressId))
                .findFirst()
                .map(a -> addresses.indexOf(a))
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada: " + addressId));
    }

    private static String requireName(String value, String campo) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException("El " + campo + " es obligatorio");
        }
        return value;
    }
}
