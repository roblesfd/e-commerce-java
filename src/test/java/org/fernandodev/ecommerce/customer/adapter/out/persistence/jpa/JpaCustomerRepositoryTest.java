package org.fernandodev.ecommerce.customer.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.customer.application.port.out.CustomerRepository;
import org.fernandodev.ecommerce.customer.domain.Customer;
import org.fernandodev.ecommerce.customer.domain.value_object.CustomerId;
import org.fernandodev.ecommerce.customer.domain.value_object.CustomerPreferences;
import org.fernandodev.model.value_object.Email;
import org.fernandodev.model.value_object.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class JpaCustomerRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CustomerRepository repository;

    @Test
    void guardaYRecuperaPorId() {
        Customer customer = crear(email());

        repository.save(customer);
        Customer loaded = repository.findById(customer.getId()).orElseThrow();

        assertThat(loaded.getId()).isEqualTo(customer.getId());
        assertThat(loaded.getEmail()).isEqualTo(customer.getEmail());
        assertThat(loaded.getIamUserId()).isEqualTo(customer.getIamUserId());
    }

    @Test
    void recuperaPorIamUserId() {
        UUID iamUserId = UUID.randomUUID();
        Customer customer = crear(email(), iamUserId);

        repository.save(customer);
        Customer loaded = repository.findByIamUserId(iamUserId).orElseThrow();

        assertThat(loaded.getId()).isEqualTo(customer.getId());
    }

    @Test
    void existeEmailTrasGuardar() {
        String email = email();
        Customer customer = crear(email);

        repository.save(customer);

        assertThat(repository.existsByEmail(email)).isTrue();
        assertThat(repository.existsByEmail("otro" + email)).isFalse();
    }

    @Test
    void persisteDireccionesPreferenciasYDefault() {
        String email = email();
        Customer customer = crear(email);
        customer.addAddress("Calle 1", "Madrid", null, "28001", "España");
        customer.addAddress("Calle 2", "Barcelona", null, "08001", "España");
        customer.updateProfile("Ana", "García", new Email(email), new PhoneNumber("5512345678"));
        customer.updatePreferences(new CustomerPreferences(true, "EUR"));

        repository.save(customer);
        Customer loaded = repository.findById(customer.getId()).orElseThrow();

        assertThat(loaded.getAddresses()).hasSize(2);
        assertThat(loaded.getAddresses().get(0).street()).isEqualTo("Calle 1");
        assertThat(loaded.getDefaultAddressId()).isPresent();
        assertThat(loaded.getPhoneNumber().value()).isEqualTo("5512345678");
        assertThat(loaded.getPreferences().newsletterEnabled()).isTrue();
        assertThat(loaded.getPreferences().preferredCurrencyCode()).isEqualTo("EUR");
    }

    private static String email() {
        return "ana." + UUID.randomUUID() + "@example.com";
    }

    private static Customer crear(String email) {
        return crear(email, UUID.randomUUID());
    }

    private static Customer crear(String email, UUID iamUserId) {
        return Customer.register(CustomerId.generate(), iamUserId, "Ana", "García", new Email(email), null);
    }
}
