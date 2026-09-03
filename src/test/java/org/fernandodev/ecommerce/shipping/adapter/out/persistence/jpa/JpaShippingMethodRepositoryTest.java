package org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingMethodRepository;
import org.fernandodev.ecommerce.shipping.domain.ShippingMethod;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaShippingMethodRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ShippingMethodRepository repository;

    @Test
    void guardaYRecuperaMetodo() {
        ShippingMethod method = ShippingMethod.create(ShippingMethodId.generate(), "Estándar", "DHL", usd(50));

        repository.save(method);
        ShippingMethod loaded = repository.findById(method.getId()).orElseThrow();

        assertThat(loaded.getName()).isEqualTo("Estándar");
        assertThat(loaded.getCarrier()).isEqualTo("DHL");
        assertThat(loaded.getCost()).isEqualTo(usd(50));
    }

    @Test
    void findAllDevuelveTodos() {
        repository.save(ShippingMethod.create(ShippingMethodId.generate(), "Estándar", "DHL", usd(50)));
        repository.save(ShippingMethod.create(ShippingMethodId.generate(), "Express", "FedEx", usd(90)));

        assertThat(repository.findAll()).hasSize(2);
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}