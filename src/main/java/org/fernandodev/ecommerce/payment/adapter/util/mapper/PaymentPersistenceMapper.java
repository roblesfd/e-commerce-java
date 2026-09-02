package org.fernandodev.ecommerce.payment.adapter.util.mapper;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.adapter.out.persistence.jpa.PaymentEventEmbeddable;
import org.fernandodev.ecommerce.payment.adapter.out.persistence.jpa.entity.PaymentJpaEntity;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentEvent;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Mapper manual entre el agregado de dominio {@link Payment} y la entidad JPA
 * {@link PaymentJpaEntity}. Sin lógica de negocio.
 */
public final class PaymentPersistenceMapper {

    private PaymentPersistenceMapper() {
    }

    public static PaymentJpaEntity toEntity(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setId(payment.getId().value());
        entity.setOrderId(payment.getOrderId().value());
        entity.setRequestId(payment.getRequestId());
        entity.setAmount(payment.getAmount().amount());
        entity.setCurrency(payment.getAmount().currencyCode());
        entity.setStatus(payment.getStatus());
        entity.setProviderTransactionId(payment.getProviderTransactionId());
        entity.setCreatedAt(payment.getCreatedAt());
        payment.getEvents().forEach(event -> entity.getEvents().add(toEventEmbeddable(event)));
        return entity;
    }

    public static Payment toDomain(PaymentJpaEntity entity) {
        return Payment.crear(
                PaymentId.of(entity.getId()),
                OrderId.of(entity.getOrderId()),
                entity.getRequestId(),
                Money.of(entity.getAmount(), entity.getCurrency()),
                entity.getStatus(),
                entity.getProviderTransactionId(),
                entity.getEvents().stream().map(PaymentPersistenceMapper::toDomainEvent).toList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt() == null ? entity.getCreatedAt() : entity.getUpdatedAt());
    }

    private static PaymentEventEmbeddable toEventEmbeddable(PaymentEvent event) {
        PaymentEventEmbeddable embeddable = new PaymentEventEmbeddable();
        embeddable.setType(event.type());
        embeddable.setDetail(event.detail());
        embeddable.setAt(event.at());
        return embeddable;
    }

    private static PaymentEvent toDomainEvent(PaymentEventEmbeddable embeddable) {
        return new PaymentEvent(embeddable.getType(), embeddable.getDetail(), embeddable.getAt());
    }
}