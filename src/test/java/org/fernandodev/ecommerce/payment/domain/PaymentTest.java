package org.fernandodev.ecommerce.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class PaymentTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2026-01-02T00:00:00Z");

    @Test
    void createCreaPagoPending() {
        Payment payment = pago();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getAmount()).isEqualTo(usd(1000));
        assertThat(payment.getEvents()).hasSize(1);
    }

    @Test
    void createConMontoNoPositivoLanzaExcepcion() {
        assertThatThrownBy(() -> Payment.create(PaymentId.generate(), OrderId.generate(), "req-1",
                Money.of("0", "USD"), T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void authorizeConAprobacionPasaAAuthorized() {
        Payment payment = pago();

        boolean ok = payment.authorize("txn-1", true, T1);

        assertThat(ok).isTrue();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(payment.getProviderTransactionId()).isEqualTo("txn-1");
    }

    @Test
    void authorizeConRechazoPasaAFailed() {
        Payment payment = pago();

        boolean ok = payment.authorize("txn-1", false, T1);

        assertThat(ok).isFalse();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void captureDesdeAuthorizedPasaACaptured() {
        Payment payment = pago();
        payment.authorize("txn-1", true, T1);

        boolean captured = payment.capture("txn-2", T2);

        assertThat(captured).isTrue();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
    }

    @Test
    void captureDuplicadaEsNoOp() {
        Payment payment = pago();
        payment.authorize("txn-1", true, T1);
        payment.capture("txn-2", T2);

        boolean repeated = payment.capture("txn-2", T2);

        assertThat(repeated).isFalse();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
    }

    @Test
    void captureDesdePendingLanzaExcepcion() {
        Payment payment = pago();

        assertThatThrownBy(() -> payment.capture("txn", T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void rejectDesdePendingPasaAFailed() {
        Payment payment = pago();

        payment.reject(T1);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void cancelDesdePendingOAuthorized() {
        Payment pending = pago();
        pending.cancel(T1);
        assertThat(pending.getStatus()).isEqualTo(PaymentStatus.CANCELLED);

        Payment authorized = pago();
        authorized.authorize("txn", true, T1);
        authorized.cancel(T1);
        assertThat(authorized.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    void cancelarPagoCapturadoLanzaExcepcion() {
        Payment payment = pago();
        payment.authorize("txn-1", true, T1);
        payment.capture("txn-2", T1);

        assertThatThrownBy(() -> payment.cancel(T2))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void refundDesdeCapturedPasaARefunded() {
        Payment payment = pago();
        payment.authorize("txn-1", true, T1);
        payment.capture("txn-2", T1);

        boolean refunded = payment.refund("txn-3", T2);

        assertThat(refunded).isTrue();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void refundDuplicadoEsNoOp() {
        Payment payment = pago();
        payment.authorize("txn-1", true, T1);
        payment.capture("txn-2", T1);
        payment.refund("txn-3", T2);

        boolean repeated = payment.refund("txn-3", T2);

        assertThat(repeated).isFalse();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void estadoDesconocidoSeRegistraSinCambiarEstado() {
        Payment payment = pago();
        payment.authorize("txn-1", true, T1);

        payment.recordUnknownState("estado-inesperado", T2);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(payment.getEvents()).hasSize(3);
        assertThat(payment.getEvents().get(2).type()).isEqualTo("UNKNOWN_STATE");
    }

    private static Payment pago() {
        return Payment.create(PaymentId.generate(), OrderId.generate(), "req-1", usd(1000), T1);
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}