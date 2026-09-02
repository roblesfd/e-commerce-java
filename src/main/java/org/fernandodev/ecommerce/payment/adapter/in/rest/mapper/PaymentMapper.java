package org.fernandodev.ecommerce.payment.adapter.in.rest.mapper;

import org.fernandodev.ecommerce.payment.adapter.in.rest.dto.PaymentResponse;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentEvent;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre el agregado {@link Payment} y el DTO REST.
 */
@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId().value(),
                payment.getOrderId().value(),
                payment.getRequestId(),
                toMoney(payment.getAmount()),
                payment.getStatus().name(),
                payment.getProviderTransactionId(),
                payment.getEvents().stream().map(this::toEventResponse).toList(),
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }

    private PaymentResponse.EventResponse toEventResponse(PaymentEvent event) {
        return new PaymentResponse.EventResponse(event.type(), event.detail(), event.at());
    }

    private static PaymentResponse.MoneyResponse toMoney(Money money) {
        return new PaymentResponse.MoneyResponse(money.amount(), money.currencyCode());
    }
}