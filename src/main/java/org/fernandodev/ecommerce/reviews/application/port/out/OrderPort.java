package org.fernandodev.ecommerce.reviews.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;

/**
 * Puerto hacia el contexto Order Management: verificar que el cliente compró
 * el producto (orden entregada) para marcar el review como compra verificada.
 */
public interface OrderPort {

    /**
     * Verifica si el cliente compró (orden entregada) el producto.
     */
    Optional<VerifiedPurchase> verifyPurchase(UUID customerId, ProductId productId);

    record VerifiedPurchase(OrderId orderId, UUID orderItemId) {
    }
}