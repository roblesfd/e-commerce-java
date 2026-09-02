package org.fernandodev.ecommerce.cart.adapter.out;

import org.fernandodev.ecommerce.cart.application.port.out.AvailabilityProvider;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.application.port.in.InventoryUseCases;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador que implementa {@link AvailabilityProvider} consultando el contexto
 * Inventory (puerto de entrada de su capa de aplicación).
 */
@Component
@RequiredArgsConstructor
public class InventoryAvailabilityProvider implements AvailabilityProvider {

    private final InventoryUseCases inventoryUseCases;

    @Override
    public boolean available(ProductId productId, ProductVariantId variantId, int quantity) {
        return inventoryUseCases.availableStock(productId, variantId) >= quantity;
    }
}