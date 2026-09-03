package org.fernandodev.ecommerce.returns.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnPolicyId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class ReturnPolicyTest {

    @Test
    void createCreaPoliticaActiva() {
        ReturnPolicy policy = ReturnPolicy.create(ReturnPolicyId.generate(), 30, Set.of());

        assertThat(policy.getReturnWindowDays()).isEqualTo(30);
        assertThat(policy.isActive()).isTrue();
    }

    @Test
    void isProductReturnableCuandoSinRestriccion() {
        ReturnPolicy policy = ReturnPolicy.create(ReturnPolicyId.generate(), 30, Set.of());

        assertThat(policy.isProductReturnable(ProductId.generate())).isTrue();
    }

    @Test
    void isProductReturnableConRestriccion() {
        ProductId returnable = ProductId.generate();
        ReturnPolicy policy = ReturnPolicy.create(ReturnPolicyId.generate(), 30, Set.of(returnable));

        assertThat(policy.isProductReturnable(returnable)).isTrue();
        assertThat(policy.isProductReturnable(ProductId.generate())).isFalse();
    }

    @Test
    void activarYDesactivarCambianElEstado() {
        ReturnPolicy policy = ReturnPolicy.create(ReturnPolicyId.generate(), 30, Set.of());

        policy.deactivate();
        assertThat(policy.isActive()).isFalse();

        policy.activate();
        assertThat(policy.isActive()).isTrue();
    }

    @Test
    void createConVentanaInvalidaLanzaExcepcion() {
        assertThatThrownBy(() -> ReturnPolicy.create(ReturnPolicyId.generate(), 0, Set.of()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}