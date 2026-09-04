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

    /**
     * Registra un nuevo cliente vinculado al usuario IAM.
     *
     * @param iamUserId identificador del usuario IAM.
     * @param profile   datos del perfil.
     * @return el cliente registrado.
     * @throws org.fernandodev.ecommerce.shared.domain.exception.ConflictException
     *         si ya existe un cliente con el mismo correo.
     */
    Customer register(UUID iamUserId, Profile profile);

    /**
     * @param iamUserId identificador del usuario IAM.
     * @return el cliente vinculado a ese usuario.
     * @throws org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException
     *         si no existe.
     */
    Customer getByIamUserId(UUID iamUserId);

    /**
     * Actualiza el perfil del cliente.
     *
     * @param iamUserId identificador del usuario IAM.
     * @param profile   nuevos datos del perfil.
     * @return el cliente actualizado.
     */
    Customer updateProfile(UUID iamUserId, Profile profile);

    /**
     * Añade una dirección al cliente.
     *
     * @param iamUserId identificador del usuario IAM.
     * @param address   datos de la dirección.
     * @return el cliente actualizado.
     */
    Customer addAddress(UUID iamUserId, AddressCommand address);

    /**
     * Actualiza una dirección del cliente.
     *
     * @param iamUserId identificador del usuario IAM.
     * @param addressId identificador de la dirección.
     * @param address   nuevos datos de la dirección.
     * @return el cliente actualizado.
     */
    Customer updateAddress(UUID iamUserId, UUID addressId, AddressCommand address);

    /**
     * Elimina una dirección del cliente.
     *
     * @param iamUserId identificador del usuario IAM.
     * @param addressId identificador de la dirección.
     * @return el cliente actualizado.
     */
    Customer removeAddress(UUID iamUserId, UUID addressId);

    /**
     * Establece la dirección predeterminada del cliente.
     *
     * @param iamUserId identificador del usuario IAM.
     * @param addressId identificador de la dirección.
     * @return el cliente actualizado.
     */
    Customer setDefaultAddress(UUID iamUserId, UUID addressId);

    /**
     * Actualiza las preferencias del cliente.
     *
     * @param iamUserId   identificador del usuario IAM.
     * @param preferences nuevas preferencias.
     * @return el cliente actualizado.
     */
    Customer updatePreferences(UUID iamUserId, PreferencesCommand preferences);

    /**
     * @param iamUserId identificador del usuario IAM.
     * @return las direcciones del cliente.
     */
    List<Address> listAddresses(UUID iamUserId);

    /**
     * @return todos los clientes (uso administrativo).
     */
    List<Customer> listAll();
}
