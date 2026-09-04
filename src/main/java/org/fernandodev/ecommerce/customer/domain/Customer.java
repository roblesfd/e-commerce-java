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

    /**
     * Factoría completa con copia defensiva de las direcciones. Usada por la
     * persistencia para reconstruir el agregado.
     *
     * @param id                identificador del cliente.
     * @param iamUserId         identificador del usuario IAM vinculado.
     * @param firstName         nombre.
     * @param lastName          apellido.
     * @param email             correo electrónico.
     * @param phoneNumber       teléfono (opcional).
     * @param addresses         direcciones del cliente.
     * @param defaultAddressId  dirección predeterminada (null si no hay).
     * @param preferences       preferencias del cliente.
     * @return el agregado {@link Customer} reconstruido.
     * @throws NullPointerException          si algún parámetro obligatorio es {@code null}.
     * @throws BusinessRuleViolationException si el nombre o el apellido están en blanco.
     */
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
     * Registra un nuevo cliente vinculado a un usuario IAM, sin direcciones y con
     * las preferencias por defecto.
     *
     * @param id          identificador del cliente.
     * @param iamUserId   identificador del usuario IAM vinculado.
     * @param firstName   nombre.
     * @param lastName    apellido.
     * @param email       correo electrónico.
     * @param phoneNumber teléfono (opcional).
     * @return el cliente registrado.
     * @throws BusinessRuleViolationException si el nombre o el apellido están en blanco.
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

    /**
     * @return el nombre completo del cliente ({@code nombre + " " + apellido}).
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * @return las direcciones del cliente como lista inmutable.
     */
    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    /**
     * @return la dirección predeterminada, o {@code Optional.empty()} si no hay ninguna.
     */
    public Optional<AddressId> getDefaultAddressId() {
        return Optional.ofNullable(defaultAddressId);
    }

    /**
     * Actualiza el perfil del cliente.
     *
     * @param firstName   nombre.
     * @param lastName    apellido.
     * @param email       correo electrónico.
     * @param phoneNumber teléfono (opcional).
     * @throws BusinessRuleViolationException si el nombre o el apellido están en blanco.
     * @throws NullPointerException           si el correo es {@code null}.
     */
    public void updateProfile(String firstName, String lastName, Email email, PhoneNumber phoneNumber) {
        this.firstName = requireName(firstName, "nombre");
        this.lastName = requireName(lastName, "apellido");
        this.email = Objects.requireNonNull(email, "correo no puede ser null");
        this.phoneNumber = phoneNumber;
    }

    /**
     * Actualiza las preferencias del cliente.
     *
     * @param preferences nuevas preferencias.
     * @throws NullPointerException si {@code preferences} es {@code null}.
     */
    public void updatePreferences(CustomerPreferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferencias no puede ser null");
    }

    /**
     * Añade una dirección. La primera dirección pasa a ser la predeterminada.
     *
     * @param street     calle.
     * @param city       ciudad.
     * @param state      estado/provincia (opcional).
     * @param postalCode código postal.
     * @param country    país.
     * @return la dirección creada.
     * @throws IllegalArgumentException si algún campo de la dirección no es válido.
     */
    public Address addAddress(String street, String city, String state, String postalCode, String country) {
        Address address = new Address(AddressId.generate(), street, city, state, postalCode, country);
        addresses.add(address);
        if (defaultAddressId == null) {
            defaultAddressId = address.id();
        }
        return address;
    }

    /**
     * Reemplaza la dirección indicada por una nueva con los datos dados.
     *
     * @param addressId  identificador de la dirección a actualizar.
     * @param street     calle.
     * @param city       ciudad.
     * @param state      estado/provincia (opcional).
     * @param postalCode código postal.
     * @param country    país.
     * @throws ResourceNotFoundException si la dirección no pertenece al cliente.
     */
    public void updateAddress(AddressId addressId, String street, String city, String state, String postalCode, String country) {
        int index = indexOf(addressId);
        addresses.set(index, new Address(addressId, street, city, state, postalCode, country));
    }

    /**
     * Elimina la dirección indicada. Si era la predeterminada, se reasigna a la
     * primera restante (o queda sin predeterminada si no quedan direcciones).
     *
     * @param addressId identificador de la dirección a eliminar.
     * @throws ResourceNotFoundException si la dirección no pertenece al cliente.
     */
    public void removeAddress(AddressId addressId) {
        int index = indexOf(addressId);
        addresses.remove(index);
        if (addressId.equals(defaultAddressId)) {
            defaultAddressId = addresses.isEmpty() ? null : addresses.get(0).id();
        }
    }

    /**
     * Establece la dirección predeterminada del cliente.
     *
     * @param addressId identificador de la dirección.
     * @throws ResourceNotFoundException si la dirección no pertenece al cliente.
     */
    public void setDefaultAddress(AddressId addressId) {
        indexOf(addressId);
        defaultAddressId = addressId;
    }

    /**
     * Busca el índice de la dirección indicada.
     *
     * @param addressId identificador de la dirección.
     * @return el índice de la dirección en la lista.
     * @throws ResourceNotFoundException si la dirección no pertenece al cliente.
     */
    private int indexOf(AddressId addressId) {

        return addresses.stream()
                .filter(a -> a.id().equals(addressId))
                .findFirst()
                .map(a -> addresses.indexOf(a))
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada: " + addressId));
    }

    /**
     * Valida que un nombre/apellido no sea {@code null} ni esté en blanco.
     *
     * @param value valor a validar.
     * @param campo nombre del campo para el mensaje de error.
     * @return el valor si es válido.
     * @throws BusinessRuleViolationException si el valor está en blanco.
     */
    private static String requireName(String value, String campo) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException("El " + campo + " es obligatorio");
        }
        return value;
    }
}
