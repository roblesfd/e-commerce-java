package org.fernandodev.ecommerce.payment.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.application.port.in.PaymentUseCases;
import org.fernandodev.ecommerce.payment.application.port.out.PaymentGateway;
import org.fernandodev.ecommerce.payment.application.port.out.PaymentRepository;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación del contexto Payments.
 * <p>
 * Aplica transiciones de estado idempotentes bajo concurrencia (lock pesimista)
 * y maneja resultados desconocidos del proveedor sin asumir éxito ni fallo.
 * </p>
 */
@RequiredArgsConstructor
public class PaymentService implements PaymentUseCases {

    private final PaymentRepository payments;
    private final PaymentGateway gateway;

    @Override
    @Transactional
    public Payment initiate(OrderId orderId, Money amount, String requestId) {
        if (payments.existsByRequestId(requestId)) {
            return payments.findByOrderId(orderId)
                    .orElseThrow(() -> new ConflictException("Ya existe un pago con requestId: " + requestId));
        }
        Payment payment = Payment.create(PaymentId.generate(), orderId, requestId, amount, Instant.now());
        payment = payments.save(payment);
        applyAuthorizeResult(payment, gateway.authorize(payment.getId().value().toString(), amount));
        return payments.save(payment);
    }

    @Override
    @Transactional
    public Payment authorize(PaymentId id) {
        Payment payment = lockExisting(id);
        applyAuthorizeResult(payment, gateway.authorize(payment.getId().value().toString(), payment.getAmount()));
        return payments.save(payment);
    }

    @Override
    @Transactional
    public Payment capture(PaymentId id) {
        Payment payment = lockExisting(id);
        PaymentGateway.GatewayResult result = gateway.capture(
                payment.getProviderTransactionId() == null ? payment.getId().value().toString() : payment.getProviderTransactionId(),
                payment.getAmount());
        if (result.outcome() == PaymentGateway.GatewayOutcome.SUCCESS) {
            payment.capture(result.transactionId(), Instant.now());
        } else {
            payment.recordUnknownState("captura no confirmada por el proveedor: " + result.outcome(), Instant.now());
        }
        return payments.save(payment);
    }

    @Override
    @Transactional
    public Payment reject(PaymentId id) {
        Payment payment = lockExisting(id);
        payment.reject(Instant.now());
        return payments.save(payment);
    }

    @Override
    @Transactional
    public Payment cancel(PaymentId id) {
        Payment payment = lockExisting(id);
        String transactionId = payment.getProviderTransactionId() == null
                ? payment.getId().value().toString() : payment.getProviderTransactionId();
        PaymentGateway.GatewayResult result = gateway.cancel(transactionId, payment.getAmount());
        if (result.outcome() == PaymentGateway.GatewayOutcome.SUCCESS) {
            payment.cancel(Instant.now());
        } else {
            payment.recordUnknownState("cancelación no confirmada por el proveedor: " + result.outcome(), Instant.now());
        }
        return payments.save(payment);
    }

    @Override
    @Transactional
    public Payment refund(PaymentId id, Money amount) {
        Payment payment = lockExisting(id);
        PaymentGateway.GatewayResult result = gateway.refund(
                payment.getProviderTransactionId() == null ? payment.getId().value().toString() : payment.getProviderTransactionId(),
                amount);
        if (result.outcome() == PaymentGateway.GatewayOutcome.SUCCESS) {
            payment.refund(result.transactionId(), amount, Instant.now());
        } else {
            payment.recordUnknownState("reembolso no confirmado por el proveedor: " + result.outcome(), Instant.now());
        }
        return payments.save(payment);
    }

    @Override
    public Payment getById(PaymentId id) {
        return payments.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + id));
    }

    @Override
    public Optional<Payment> getByOrder(OrderId orderId) {
        return payments.findByOrderId(orderId);
    }

    @Override
    public List<Payment> listAll() {
        return payments.findAll();
    }

    @Override
    @Transactional
    public Payment processWebhook(String transactionId, String providerStatus) {
        Payment payment = payments.findByProviderTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado para transacción: " + transactionId));
        applyWebhook(payment, providerStatus);
        return payments.save(payment);
    }

    private void applyWebhook(Payment payment, String providerStatus) {
        Instant now = Instant.now();
        switch (providerStatus.toLowerCase()) {
            case "authorized" -> {
                if (payment.getStatus() == org.fernandodev.ecommerce.payment.domain.value_object.PaymentStatus.PENDING) {
                    payment.authorize(payment.getProviderTransactionId(), true, now);
                }
            }
            case "captured" -> payment.capture(payment.getProviderTransactionId(), now);
            case "refunded" -> {
                Money remaining = payment.getAmount().subtract(
                        payment.getRefundedAmount() == null
                                ? Money.zero(payment.getAmount().currencyCode())
                                : payment.getRefundedAmount());
                payment.refund(payment.getProviderTransactionId(), remaining, now);
            }
            case "failed" -> {
                if (payment.getStatus() == org.fernandodev.ecommerce.payment.domain.value_object.PaymentStatus.PENDING) {
                    payment.authorize(payment.getProviderTransactionId(), false, now);
                } else {
                    payment.recordUnknownState("fallo tardío del proveedor", now);
                }
            }
            case "cancelled" -> {
                if (payment.getStatus() == org.fernandodev.ecommerce.payment.domain.value_object.PaymentStatus.PENDING
                        || payment.getStatus() == org.fernandodev.ecommerce.payment.domain.value_object.PaymentStatus.AUTHORIZED) {
                    payment.cancel(now);
                }
            }
            default -> payment.recordUnknownState("estado de proveedor desconocido: " + providerStatus, now);
        }
    }

    private void applyAuthorizeResult(Payment payment, PaymentGateway.GatewayResult result) {
        switch (result.outcome()) {
            case SUCCESS -> payment.authorize(result.transactionId(), true, Instant.now());
            case FAILURE -> payment.authorize(result.transactionId(), false, Instant.now());
            case UNKNOWN -> payment.recordUnknownState("resultado de autorización desconocido", Instant.now());
        }
    }

    private Payment lockExisting(PaymentId id) {
        return payments.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + id));
    }
}