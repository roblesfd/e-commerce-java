package org.fernandodev.ecommerce.customer.adapter.util.mapper;

import org.fernandodev.ecommerce.customer.adapter.out.persistence.jpa.CustomerAddressEmbeddable;
import org.fernandodev.ecommerce.customer.adapter.out.persistence.jpa.entity.CustomerJpaEntity;
import org.fernandodev.ecommerce.customer.domain.value_object.Address;
import org.fernandodev.ecommerce.customer.domain.value_object.AddressId;
import org.fernandodev.ecommerce.customer.domain.Customer;
import org.fernandodev.ecommerce.customer.domain.value_object.CustomerId;
import org.fernandodev.ecommerce.customer.domain.value_object.CustomerPreferences;
import org.fernandodev.model.value_object.Email;
import org.fernandodev.model.value_object.PhoneNumber;

/**
 * Mapper manual entre el agregado de dominio {@link Customer} y la entidad
 * JPA {@link CustomerJpaEntity}. Sin lógica de negocio.
 */
public final class CustomerPersistenceMapper {

    private CustomerPersistenceMapper() {
    }

    /**
     * @param customer agregado de dominio.
     * @return entidad JPA equivalente.
     */
    public static CustomerJpaEntity toEntity(Customer customer) {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.setId(customer.getId().value());
        entity.setIamUserId(customer.getIamUserId());
        entity.setFirstName(customer.getFirstName());
        entity.setLastName(customer.getLastName());
        entity.setEmail(customer.getEmail().direccion());
        entity.setPhoneNumber(customer.getPhoneNumber() == null ? null : customer.getPhoneNumber().value());
        entity.setDefaultAddressId(customer.getDefaultAddressId().map(AddressId::value).orElse(null));
        entity.setNewsletterEnabled(customer.getPreferences().newsletterEnabled());
        entity.setPreferredCurrency(customer.getPreferences().preferredCurrencyCode());
        customer.getAddresses().forEach(address -> entity.getAddresses().add(toEmbeddable(address)));
        return entity;
    }

    /**
     * @param entity entidad JPA.
     * @return agregado de dominio equivalente.
     */
    public static Customer toDomain(CustomerJpaEntity entity) {
        return Customer.crear(
                CustomerId.of(entity.getId()),
                entity.getIamUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                new Email(entity.getEmail()),
                entity.getPhoneNumber() == null ? null : new PhoneNumber(entity.getPhoneNumber()),
                entity.getAddresses().stream().map(CustomerPersistenceMapper::toDomain).toList(),
                entity.getDefaultAddressId() == null ? null : AddressId.of(entity.getDefaultAddressId()),
                new CustomerPreferences(entity.isNewsletterEnabled(), entity.getPreferredCurrency()));
    }

    private static CustomerAddressEmbeddable toEmbeddable(Address address) {
        CustomerAddressEmbeddable embeddable = new CustomerAddressEmbeddable();
        embeddable.setId(address.id().value());
        embeddable.setStreet(address.street());
        embeddable.setCity(address.city());
        embeddable.setState(address.state());
        embeddable.setPostalCode(address.postalCode());
        embeddable.setCountry(address.country());
        return embeddable;
    }

    private static Address toDomain(CustomerAddressEmbeddable embeddable) {
        return new Address(
                AddressId.of(embeddable.getId()),
                embeddable.getStreet(),
                embeddable.getCity(),
                embeddable.getState(),
                embeddable.getPostalCode(),
                embeddable.getCountry());
    }
}
