package org.fernandodev.ecommerce.shipping.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class ShippingMethodTest {

    @Test
    void createCreaMetodoActivo() {
        ShippingMethod method = ShippingMethod.create(ShippingMethodId.generate(), "Estándar", "DHL", usd(50));

        assertThat(method.getName()).isEqualTo("Estándar");
        assertThat(method.getCarrier()).isEqualTo("DHL");
        assertThat(method.getCost()).isEqualTo(usd(50));
        assertThat(method.isActive()).isTrue();
    }

    @Test
    void activarYDesactivarCambianElEstado() {
        ShippingMethod method = ShippingMethod.create(ShippingMethodId.generate(), "Estándar", "DHL", usd(50));

        method.deactivate();
        assertThat(method.isActive()).isFalse();

        method.activate();
        assertThat(method.isActive()).isTrue();
    }

    @Test
    void createConCostoNegativoLanzaExcepcion() {
        assertThatThrownBy(() -> ShippingMethod.create(ShippingMethodId.generate(), "Estándar", "DHL",
                Money.of("-5", "USD")))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void createConNombreVacioLanzaExcepcion() {
        assertThatThrownBy(() -> ShippingMethod.create(ShippingMethodId.generate(), "  ", "DHL", usd(50)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}