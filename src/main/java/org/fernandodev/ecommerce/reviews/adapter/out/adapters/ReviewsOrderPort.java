package org.fernandodev.ecommerce.reviews.adapter.out.adapters;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.order.domain.value_object.OrderStatus;
import org.fernandodev.ecommerce.reviews.application.port.out.OrderPort;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador del puerto {@link OrderPort} hacia el contexto Order Management.
 * Verifica que el cliente tenga una orden entregada con el producto.
 */
@Component
@RequiredArgsConstructor
public class ReviewsOrderPort implements OrderPort {

    private final OrderUseCases orders;

    @Override
    public Optional<VerifiedPurchase> verifyPurchase(UUID customerId, ProductId productId) {
        return orders.listByCustomer(customerId).stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .flatMap(order -> order.getItems().stream()
                        .filter(item -> item.productId().equals(productId))
                        .map(item -> new VerifiedPurchase(order.getId(), item.id().value())))
                .findFirst();
    }
}