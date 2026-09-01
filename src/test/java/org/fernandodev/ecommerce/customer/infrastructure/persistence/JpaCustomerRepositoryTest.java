package org.fernandodev.ecommerce.customer.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.customer.application.port.out.CustomerRepository;
import org.fernandodev.ecommerce.customer.domain.Customer;
import org.fernandodev.ecommerce.customer.domain.CustomerId;
import org.fernandodev.ecommerce.customer.domain.Email;
import org.fernandodev.ecommerce.customer.domain.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class JpaCustomerRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CustomerRepository repository;

    @Test
    void guardaYRecuperaPorId() {
        Customer customer = crear(email());

        repository.save(customer);
        Customer loaded = repository.findById(customer.id()).orElseThrow();

        assertThat(loaded.id()).isEqualTo(customer.id());
        assertThat(loaded.email()).isEqualTo(customer.email());
        assertThat(loaded.iamUserId()).isEqualTo(customer.iamUserId());
    }

    @Test
    void recuperaPorIamUserId() {
        UUID iamUserId = UUID.randomUUID();
        Customer customer = crear(email(), iamUserId);

        repository.save(customer);
        Customer loaded = repository.findByIamUserId(iamUserId).orElseThrow();

        assertThat(loaded.id()).isEqualTo(customer.id());
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
        customer.updateProfile("Ana", "García", new Email(email), new PhoneNumber("+34600000000"));
        customer.updatePreferences(new org.fernandodev.ecommerce.customer.domain.CustomerPreferences(true, "EUR"));

        repository.save(customer);
        Customer loaded = repository.findById(customer.id()).orElseThrow();

        assertThat(loaded.addresses()).hasSize(2);
        assertThat(loaded.addresses().get(0).street()).isEqualTo("Calle 1");
        assertThat(loaded.defaultAddressId()).isPresent();
        assertThat(loaded.phoneNumber().value()).isEqualTo("+34600000000");
        assertThat(loaded.preferences().newsletterEnabled()).isTrue();
        assertThat(loaded.preferences().preferredCurrencyCode()).isEqualTo("EUR");
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