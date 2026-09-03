package org.fernandodev.ecommerce.search.adapter.out.adapters;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.application.port.in.InventoryUseCases;
import org.fernandodev.ecommerce.search.application.port.out.InventoryPort;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador del puerto {@link InventoryPort} hacia el contexto Inventory.
 */
@Component
@RequiredArgsConstructor
public class SearchInventoryPort implements InventoryPort {

    private final InventoryUseCases inventory;

    @Override
    public int availableStock(ProductId productId, ProductVariantId variantId) {
        return inventory.availableStock(productId, variantId);
    }
}