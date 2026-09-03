package org.fernandodev.ecommerce.payment.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.adapter.out.persistence.jpa.entity.PaymentJpaEntity;
import org.fernandodev.ecommerce.payment.adapter.util.mapper.PaymentPersistenceMapper;
import org.fernandodev.ecommerce.payment.application.port.out.PaymentRepository;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link PaymentRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaPaymentRepository implements PaymentRepository {

    private final PaymentSpringDataRepository springData;

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity saved = springData.save(PaymentPersistenceMapper.toEntity(payment));
        return PaymentPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        return springData.findById(id.value()).map(PaymentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByIdForUpdate(PaymentId id) {
        return springData.findByIdForUpdate(id.value()).map(PaymentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(OrderId orderId) {
        return springData.findByOrderId(orderId.value()).map(PaymentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByProviderTransactionId(String transactionId) {
        return springData.findByProviderTransactionId(transactionId).map(PaymentPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByRequestId(String requestId) {
        return springData.existsByRequestId(requestId);
    }

    @Override
    public List<Payment> findAll() {
        return springData.findAllByOrderByCreatedAtDesc().stream()
                .map(PaymentPersistenceMapper::toDomain)
                .toList();
    }
}