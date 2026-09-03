package org.fernandodev.ecommerce.customer.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.customer.application.dto.AddressCommand;
import org.fernandodev.ecommerce.customer.application.dto.PreferencesCommand;
import org.fernandodev.ecommerce.customer.application.dto.Profile;
import org.fernandodev.ecommerce.customer.application.port.out.CustomerRepository;
import org.fernandodev.ecommerce.customer.domain.Customer;
import org.fernandodev.ecommerce.customer.domain.value_object.CustomerId;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class CustomerServiceTest {

    private final FakeCustomerRepository repository = new FakeCustomerRepository();
    private final CustomerService service = new CustomerService(repository);

    @Test
    void registraClienteYLoGuarda() {
        UUID iamUserId = UUID.randomUUID();

        Customer customer = service.register(iamUserId, new Profile("Ana", "García", "ana@example.com", null));

        assertThat(customer.getIamUserId()).isEqualTo(iamUserId);
        assertThat(repository.findByIamUserId(iamUserId)).contains(customer);
    }

    @Test
    void listAllDevuelveTodosLosClientes() {
        service.register(UUID.randomUUID(), new Profile("Ana", "García", "ana@example.com", null));
        service.register(UUID.randomUUID(), new Profile("Luis", "Pérez", "luis@example.com", null));

        assertThat(service.listAll()).hasSize(2);
    }

    @Test
    void registroConEmailDuplicadoLanzaConflicto() {
        service.register(UUID.randomUUID(), new Profile("Ana", "García", "ana@example.com", null));

        assertThatThrownBy(() -> service.register(UUID.randomUUID(), new Profile("Luis", "Pérez", "ana@example.com", null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getByIamUserIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getByIamUserId(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addAddressPersisteLaDireccionComoPredeterminada() {
        Customer customer = service.register(UUID.randomUUID(), new Profile("Ana", "García", "ana@example.com", null));

        Customer actualizado = service.addAddress(customer.getIamUserId(),
                new AddressCommand("Calle 1", "Madrid", null, "28001", "España"));

        assertThat(actualizado.getAddresses()).hasSize(1);
        assertThat(actualizado.getDefaultAddressId()).isPresent();
    }

    @Test
    void updateProfileConEmailDeOtroClienteLanzaConflicto() {
        Customer first = service.register(UUID.randomUUID(), new Profile("Ana", "García", "ana@example.com", null));
        service.register(UUID.randomUUID(), new Profile("Luis", "Pérez", "luis@example.com", null));

        assertThatThrownBy(() -> service.updateProfile(first.getIamUserId(),
                new Profile("Ana", "García", "luis@example.com", null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updatePreferencesAplicaCambios() {
        Customer customer = service.register(UUID.randomUUID(), new Profile("Ana", "García", "ana@example.com", null));

        Customer actualizado = service.updatePreferences(customer.getIamUserId(), new PreferencesCommand(true, "EUR"));

        assertThat(actualizado.getPreferences().newsletterEnabled()).isTrue();
        assertThat(actualizado.getPreferences().preferredCurrencyCode()).isEqualTo("EUR");
    }

    private static final class FakeCustomerRepository implements CustomerRepository {

        private final Map<CustomerId, Customer> byId = new HashMap<>();
        private final Map<UUID, CustomerId> byIamUserId = new HashMap<>();

        @Override
        public Optional<Customer> findById(CustomerId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Customer> findByIamUserId(UUID iamUserId) {
            CustomerId id = byIamUserId.get(iamUserId);
            return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
        }

        @Override
        public boolean existsByEmail(String email) {
            return byId.values().stream().anyMatch(c -> c.getEmail().direccion().equals(email));
        }

        @Override
        public Customer save(Customer customer) {
            byId.put(customer.getId(), customer);
            byIamUserId.put(customer.getIamUserId(), customer.getId());
            return customer;
        }

        @Override
        public List<Customer> findAll() {
            return List.copyOf(byId.values());
        }
    }
}
