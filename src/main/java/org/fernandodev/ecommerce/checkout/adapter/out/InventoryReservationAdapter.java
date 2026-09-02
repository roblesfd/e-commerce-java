package org.fernandodev.ecommerce.checkout.adapter.out;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.checkout.application.port.out.InventoryReservationPort;
import org.fernandodev.ecommerce.inventory.application.dto.ReserveStockCommand;
import org.fernandodev.ecommerce.inventory.application.port.in.InventoryUseCases;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservationId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador que implementa {@link InventoryReservationPort} consultando el
 * contexto Inventory.
 */
@Component
@RequiredArgsConstructor
public class InventoryReservationAdapter implements InventoryReservationPort {

    private final InventoryUseCases inventoryUseCases;

    @Override
    public UUID reserve(ProductId productId, ProductVariantId variantId, int quantity, Instant expiresAt) {
        return inventoryUseCases.reserveStock(productId, variantId, new ReserveStockCommand(quantity, expiresAt))
                .id()
                .value();
    }

    @Override
    public void release(ProductId productId, ProductVariantId variantId, UUID reservationId) {
        inventoryUseCases.releaseReservation(productId, variantId, StockReservationId.of(reservationId));
    }
}