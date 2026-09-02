package org.fernandodev.ecommerce.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.application.port.out.PaymentGateway;
import org.fernandodev.ecommerce.payment.application.port.out.PaymentRepository;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentServiceTest {

    private final FakePaymentRepository repository = new FakePaymentRepository();
    private final FakePaymentGateway gateway = new FakePaymentGateway();
    private final PaymentService service = new PaymentService(repository, gateway);

    @BeforeEach
    void setUp() {
        gateway.outcome = PaymentGateway.GatewayOutcome.SUCCESS;
    }

    @Test
    void initiateCreaYAutorizaElPago() {
        Payment payment = service.initiate(OrderId.generate(), usd(1000), "req-1");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(payment.getProviderTransactionId()).isNotEmpty();
        assertThat(repository.findById(payment.getId())).contains(payment);
    }

    @Test
    void initiateIdempotentePorRequestIdDevuelveElExistente() {
        OrderId orderId = OrderId.generate();
        Payment first = service.initiate(orderId, usd(1000), "req-1");

        Payment retry = service.initiate(orderId, usd(1000), "req-1");

        assertThat(retry.getStatus()).isEqualTo(first.getStatus());
        assertThat(retry.getAmount()).isEqualTo(first.getAmount());
    }

    @Test
    void initiateConFalloDeAutorizacionMarcaFailed() {
        gateway.outcome = PaymentGateway.GatewayOutcome.FAILURE;

        Payment payment = service.initiate(OrderId.generate(), usd(1000), "req-1");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void initiateConResultadoDesconocidoMantienePending() {
        gateway.outcome = PaymentGateway.GatewayOutcome.UNKNOWN;

        Payment payment = service.initiate(OrderId.generate(), usd(1000), "req-1");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getEvents().get(payment.getEvents().size() - 1).type()).isEqualTo("UNKNOWN_STATE");
    }

    @Test
    void captureDesdeAuthorizedPasaACaptured() {
        Payment payment = autorizado();

        Payment captured = service.capture(payment.getId());

        assertThat(captured.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
    }

    @Test
    void captureDuplicadaEsNoOp() {
        Payment payment = autorizado();
        service.capture(payment.getId());

        Payment repeated = service.capture(payment.getId());

        assertThat(repeated.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
    }

    @Test
    void rejectPasaAFailed() {
        Payment payment = service.initiate(OrderId.generate(), usd(1000), "req-" + UUID.randomUUID());
        // recreate a pending payment
        Payment pending = pagoPending();

        Payment rejected = service.reject(pending.getId());

        assertThat(rejected.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void cancelDesdePendingPasaACancelled() {
        Payment payment = pagoPending();

        Payment cancelled = service.cancel(payment.getId());

        assertThat(cancelled.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    void refundDesdeCapturedPasaARefunded() {
        Payment payment = capturado();

        Payment refunded = service.refund(payment.getId());

        assertThat(refunded.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void refundDuplicadoEsNoOp() {
        Payment payment = capturado();
        service.refund(payment.getId());

        Payment repeated = service.refund(payment.getId());

        assertThat(repeated.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(PaymentId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByOrderDevuelveElPago() {
        OrderId orderId = OrderId.generate();
        Payment payment = service.initiate(orderId, usd(1000), "req-1");

        assertThat(service.getByOrder(orderId)).contains(payment);
    }

    @Test
    void processWebhookCapturedConfirmaLaCaptura() {
        Payment payment = autorizado();

        Payment processed = service.processWebhook(payment.getProviderTransactionId(), "captured");

        assertThat(processed.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
    }

    @Test
    void processWebhookEstadoDesconocidoSeRegistraSinCambiarEstado() {
        Payment payment = autorizado();

        Payment processed = service.processWebhook(payment.getProviderTransactionId(), "estado-raro");

        assertThat(processed.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(processed.getEvents().get(processed.getEvents().size() - 1).type()).isEqualTo("UNKNOWN_STATE");
    }

    private Payment pagoPending() {
        return repository.save(Payment.create(PaymentId.generate(), OrderId.generate(),
                "req-" + UUID.randomUUID(), usd(1000), java.time.Instant.now()));
    }

    private Payment autorizado() {
        Payment payment = pagoPending();
        payment.authorize("txn-" + payment.getId().value(), true, java.time.Instant.now());
        return repository.save(payment);
    }

    private Payment capturado() {
        Payment payment = autorizado();
        payment.capture("txn-capture", java.time.Instant.now());
        return repository.save(payment);
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }

    private static final class FakePaymentRepository implements PaymentRepository {

        private final Map<PaymentId, Payment> byId = new HashMap<>();

        @Override
        public Payment save(Payment payment) {
            byId.put(payment.getId(), payment);
            return payment;
        }

        @Override
        public Optional<Payment> findById(PaymentId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Payment> findByIdForUpdate(PaymentId id) {
            return findById(id);
        }

        @Override
        public Optional<Payment> findByOrderId(OrderId orderId) {
            return byId.values().stream().filter(p -> p.getOrderId().equals(orderId)).findFirst();
        }

        @Override
        public Optional<Payment> findByProviderTransactionId(String transactionId) {
            return byId.values().stream()
                    .filter(p -> transactionId.equals(p.getProviderTransactionId()))
                    .findFirst();
        }

        @Override
        public boolean existsByRequestId(String requestId) {
            return byId.values().stream().anyMatch(p -> p.getRequestId().equals(requestId));
        }
    }

    private static final class FakePaymentGateway implements PaymentGateway {

        private GatewayOutcome outcome = GatewayOutcome.SUCCESS;

        @Override
        public GatewayResult authorize(String transactionId, Money amount) {
            return new GatewayResult(transactionId, outcome);
        }

        @Override
        public GatewayResult capture(String transactionId, Money amount) {
            return new GatewayResult(transactionId, outcome);
        }

        @Override
        public GatewayResult refund(String transactionId, Money amount) {
            return new GatewayResult(transactionId, outcome);
        }

        @Override
        public GatewayResult cancel(String transactionId, Money amount) {
            return new GatewayResult(transactionId, outcome);
        }
    }
}