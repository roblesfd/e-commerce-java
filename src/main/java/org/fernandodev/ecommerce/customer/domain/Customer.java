package org.fernandodev.ecommerce.customer.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;

/**
 * Agregado raíz del contexto Customer Management.
 * <p>
 * Representa la información comercial del cliente. La identidad y credenciales
 * pertenecen al IAM; aquí se guarda la referencia ({@code iamUserId}) y la
 * información específica de comercio (perfil, direcciones, preferencias).
 * </p>
 */
public class Customer {

    private final CustomerId id;
    private final UUID iamUserId;
    private String firstName;
    private String lastName;
    private Email email;
    private PhoneNumber phoneNumber;
    private final List<Address> addresses;
    private AddressId defaultAddressId;
    private CustomerPreferences preferences;

    public Customer(
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
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.iamUserId = Objects.requireNonNull(iamUserId, "iamUserId must not be null");
        setFirstName(firstName);
        setLastName(lastName);
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.phoneNumber = phoneNumber;
        this.addresses = new ArrayList<>(addresses == null ? List.of() : addresses);
        this.defaultAddressId = defaultAddressId;
        this.preferences = Objects.requireNonNull(preferences, "preferences must not be null");
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
        return new Customer(id, iamUserId, firstName, lastName, email, phoneNumber,
                List.of(), null, CustomerPreferences.defaults());
    }

    public CustomerId id() {
        return id;
    }

    public UUID iamUserId() {
        return iamUserId;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    public Email email() {
        return email;
    }

    public PhoneNumber phoneNumber() {
        return phoneNumber;
    }

    public List<Address> addresses() {
        return Collections.unmodifiableList(addresses);
    }

    public Optional<AddressId> defaultAddressId() {
        return Optional.ofNullable(defaultAddressId);
    }

    public CustomerPreferences preferences() {
        return preferences;
    }

    public void updateProfile(String firstName, String lastName, Email email, PhoneNumber phoneNumber) {
        setFirstName(firstName);
        setLastName(lastName);
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.phoneNumber = phoneNumber;
    }

    public void updatePreferences(CustomerPreferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences must not be null");
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
        for (int i = 0; i < addresses.size(); i++) {
            if (addresses.get(i).id().equals(addressId)) {
                return i;
            }
        }
        throw new ResourceNotFoundException("Dirección no encontrada: " + addressId);
    }

    private void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new BusinessRuleViolationException("firstName es obligatorio");
        }
        this.firstName = firstName.trim();
    }

    private void setLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new BusinessRuleViolationException("lastName es obligatorio");
        }
        this.lastName = lastName.trim();
    }
}