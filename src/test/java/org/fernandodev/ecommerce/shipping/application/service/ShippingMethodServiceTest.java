package org.fernandodev.ecommerce.shipping.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.fernandodev.ecommerce.shipping.application.dto.CreateShippingMethodCommand;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingMethodRepository;
import org.fernandodev.ecommerce.shipping.domain.ShippingMethod;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class ShippingMethodServiceTest {

    private final FakeShippingMethodRepository repository = new FakeShippingMethodRepository();
    private final ShippingMethodService service = new ShippingMethodService(repository);

    @Test
    void createGuardaMetodo() {
        ShippingMethod method = service.create(new CreateShippingMethodCommand("Estándar", "DHL", usd(50)));

        assertThat(method.getId()).isNotNull();
        assertThat(method.isActive()).isTrue();
        assertThat(repository.findById(method.getId())).contains(method);
    }

    @Test
    void listDevuelveTodos() {
        service.create(new CreateShippingMethodCommand("Estándar", "DHL", usd(50)));
        service.create(new CreateShippingMethodCommand("Express", "FedEx", usd(90)));

        assertThat(service.list()).hasSize(2);
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(ShippingMethodId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void activateYDeactivateCambianElEstado() {
        ShippingMethod method = service.create(new CreateShippingMethodCommand("Estándar", "DHL", usd(50)));

        ShippingMethod desactivado = service.deactivate(method.getId());
        assertThat(desactivado.isActive()).isFalse();

        ShippingMethod activado = service.activate(method.getId());
        assertThat(activado.isActive()).isTrue();
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }

    private static final class FakeShippingMethodRepository implements ShippingMethodRepository {

        private final Map<ShippingMethodId, ShippingMethod> byId = new HashMap<>();

        @Override
        public ShippingMethod save(ShippingMethod method) {
            byId.put(method.getId(), method);
            return method;
        }

        @Override
        public Optional<ShippingMethod> findById(ShippingMethodId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<ShippingMethod> findAll() {
            return List.copyOf(byId.values());
        }

        @Override
        public boolean existsById(ShippingMethodId id) {
            return byId.containsKey(id);
        }
    }
}