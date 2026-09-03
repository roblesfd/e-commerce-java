package org.fernandodev.ecommerce.returns.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.returns.domain.value_object.InspectionResult;
import org.fernandodev.ecommerce.returns.domain.value_object.RefundStatus;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnItem;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnReason;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class ReturnTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void createCreaDevolucionRequested() {
        Return aReturn = devolucion();

        assertThat(aReturn.getStatus()).isEqualTo(ReturnStatus.REQUESTED);
        assertThat(aReturn.getItems()).hasSize(1);
        assertThat(aReturn.isTerminal()).isFalse();
        assertThat(aReturn.getStatusHistory()).hasSize(1);
    }

    @Test
    void createSinItemsLanzaExcepcion() {
        assertThatThrownBy(() -> Return.create(ReturnId.generate(), OrderId.generate(), UUID.randomUUID(),
                ReturnReason.DEFECTIVE, List.of(), T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void cicloHastaCompletadoConInspeccion() {
        Return aReturn = devolucion();

        aReturn.approve(usd(900), T1);
        aReturn.registerReturnShipment("RTN-1", T1);
        aReturn.receive(T1);
        aReturn.inspect(new InspectionResult("BUENO", "sin daños", true, T1), T1);
        aReturn.complete(T1);

        assertThat(aReturn.getStatus()).isEqualTo(ReturnStatus.COMPLETED);
        assertThat(aReturn.isTerminal()).isTrue();
        assertThat(aReturn.getStatusHistory()).hasSize(6);
        assertThat(aReturn.getInspection().passed()).isTrue();
    }

    @Test
    void transicionesInvalidasLanzanExcepcion() {
        Return aReturn = devolucion();

        assertThatThrownBy(() -> aReturn.receive(T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void cancelDesdeRequestedOApprovedEsValido() {
        Return requested = devolucion();
        requested.cancel(T1);
        assertThat(requested.getStatus()).isEqualTo(ReturnStatus.CANCELLED);

        Return approved = devolucion();
        approved.approve(usd(900), T1);
        approved.cancel(T1);
        assertThat(approved.getStatus()).isEqualTo(ReturnStatus.CANCELLED);
    }

    @Test
    void refundSeTrackeaYEsReintentableTrasFallo() {
        Return aReturn = devolucion();
        aReturn.approve(usd(900), T1);
        aReturn.registerReturnShipment("RTN-1", T1);
        aReturn.receive(T1);
        aReturn.inspect(new InspectionResult("BUENO", null, true, T1), T1);
        aReturn.complete(T1);

        assertThat(aReturn.canRetryRefund()).isFalse();

        aReturn.markRefundRequested(T1);
        assertThat(aReturn.getRefundStatus()).isEqualTo(RefundStatus.PENDING);
        assertThat(aReturn.canRetryRefund()).isFalse();

        aReturn.markRefundFailed(T1);
        assertThat(aReturn.canRetryRefund()).isTrue();

        aReturn.markRefundSucceeded(T1);
        assertThat(aReturn.getRefundStatus()).isEqualTo(RefundStatus.REFUNDED);
        assertThat(aReturn.canRetryRefund()).isFalse();
    }

    @Test
    void cancelarDevolucionCompletadaLanzaExcepcion() {
        Return aReturn = devolucion();
        aReturn.approve(usd(900), T1);
        aReturn.registerReturnShipment("RTN-1", T1);
        aReturn.receive(T1);
        aReturn.inspect(new InspectionResult("BUENO", null, true, T1), T1);
        aReturn.complete(T1);

        assertThatThrownBy(() -> aReturn.cancel(T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private static Return devolucion() {
        return Return.create(ReturnId.generate(), OrderId.generate(), UUID.randomUUID(), ReturnReason.DEFECTIVE,
                List.of(new ReturnItem(UUID.randomUUID(), ProductId.generate(), 1, usd(900))), T1);
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}