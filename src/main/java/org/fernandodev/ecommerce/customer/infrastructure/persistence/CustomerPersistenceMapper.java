package org.fernandodev.ecommerce.customer.infrastructure.persistence;

import org.fernandodev.ecommerce.customer.domain.Address;
import org.fernandodev.ecommerce.customer.domain.AddressId;
import org.fernandodev.ecommerce.customer.domain.Customer;
import org.fernandodev.ecommerce.customer.domain.CustomerId;
import org.fernandodev.ecommerce.customer.domain.CustomerPreferences;
import org.fernandodev.ecommerce.customer.domain.Email;
import org.fernandodev.ecommerce.customer.domain.PhoneNumber;

/**
 * Mapper manual entre el agregado de dominio {@link Customer} y la entidad
 * JPA {@link CustomerJpaEntity}. Sin lógica de negocio.
 */
public final class CustomerPersistenceMapper {

    private CustomerPersistenceMapper() {
    }

    public static CustomerJpaEntity toEntity(Customer customer) {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.setId(customer.id().value());
        entity.setIamUserId(customer.iamUserId());
        entity.setFirstName(customer.firstName());
        entity.setLastName(customer.lastName());
        entity.setEmail(customer.email().value());
        entity.setPhoneNumber(customer.phoneNumber() == null ? null : customer.phoneNumber().value());
        entity.setDefaultAddressId(customer.defaultAddressId().map(AddressId::value).orElse(null));
        entity.setNewsletterEnabled(customer.preferences().newsletterEnabled());
        entity.setPreferredCurrency(customer.preferences().preferredCurrencyCode());
        customer.addresses().forEach(address -> entity.getAddresses().add(toEmbeddable(address)));
        return entity;
    }

    public static Customer toDomain(CustomerJpaEntity entity) {
        return new Customer(
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