package org.fernandodev.ecommerce.customer.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class CustomerTest {

    @Test
    void registrarCreaClienteConPreferenciasPorDefecto() {
        Customer customer = cliente();

        assertThat(customer.preferences().preferredCurrencyCode()).isEqualTo("USD");
        assertThat(customer.addresses()).isEmpty();
        assertThat(customer.defaultAddressId()).isEmpty();
        assertThat(customer.fullName()).isEqualTo("Ana García");
    }

    @Test
    void primeraDireccionSeConvierteEnPredeterminada() {
        Customer customer = cliente();
        Address a1 = customer.addAddress("Calle 1", "Madrid", null, "28001", "España");
        customer.addAddress("Calle 2", "Barcelona", null, "08001", "España");

        assertThat(customer.defaultAddressId()).contains(a1.id());
        assertThat(customer.addresses()).hasSize(2);
    }

    @Test
    void setDefaultAddressCambiaLaPredeterminada() {
        Customer customer = cliente();
        Address a1 = customer.addAddress("Calle 1", "Madrid", null, "28001", "España");
        Address a2 = customer.addAddress("Calle 2", "Barcelona", null, "08001", "España");

        customer.setDefaultAddress(a2.id());

        assertThat(customer.defaultAddressId()).contains(a2.id());
    }

    @Test
    void eliminarDireccionPredeterminadaReasignaLaPrimera() {
        Customer customer = cliente();
        Address a1 = customer.addAddress("Calle 1", "Madrid", null, "28001", "España");
        Address a2 = customer.addAddress("Calle 2", "Barcelona", null, "08001", "España");
        customer.setDefaultAddress(a2.id());

        customer.removeAddress(a2.id());

        assertThat(customer.defaultAddressId()).contains(a1.id());
        assertThat(customer.addresses()).hasSize(1);
    }

    @Test
    void eliminarDireccionInexistenteLanzaExcepcion() {
        Customer customer = cliente();

        assertThatThrownBy(() -> customer.removeAddress(AddressId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void actualizarPerfilCambiaDatos() {
        Customer customer = cliente();

        customer.updateProfile("María", "López", new Email("maria@example.com"), new PhoneNumber("+34600000000"));

        assertThat(customer.firstName()).isEqualTo("María");
        assertThat(customer.email().value()).isEqualTo("maria@example.com");
        assertThat(customer.phoneNumber().value()).isEqualTo("+34600000000");
    }

    @Test
    void emailInvalidoEsRechazado() {
        assertThatThrownBy(() -> new Email("no-es-email"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nombreObligatorioEsRechazado() {
        assertThatThrownBy(() -> Customer.register(CustomerId.generate(), UUID.randomUUID(), "", "García",
                new Email("ana@example.com"), null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private static Customer cliente() {
        return Customer.register(CustomerId.generate(), UUID.randomUUID(), "Ana", "García",
                new Email("ana@example.com"), null);
    }
}