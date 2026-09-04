package org.fernandodev.ecommerce.admin.application.service;

import java.util.List;

import org.fernandodev.ecommerce.admin.application.port.in.AdminInventoryUseCases;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.application.dto.AdjustStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.DecreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.IncreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.port.in.InventoryUseCases;
import org.fernandodev.ecommerce.inventory.domain.Inventory;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockMovement;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Orquestación administrativa de inventario.
 */
@RequiredArgsConstructor
public class AdminInventoryService implements AdminInventoryUseCases {

    private final InventoryUseCases inventory;
    private final AuditService audit;

    /**

     * {@inheritDoc}

     */

    @Override
    public Inventory getInventory(ProductId productId, ProductVariantId variantId) {
        return inventory.getByProduct(productId, variantId);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Inventory increaseStock(ProductId productId, ProductVariantId variantId, IncreaseStockCommand command) {
        audit.record("INCREASE_STOCK", "inventory", productId.value(),
                "Entrada de stock: " + command.quantity());
        return inventory.increaseStock(productId, variantId, command);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Inventory decreaseStock(ProductId productId, ProductVariantId variantId, DecreaseStockCommand command) {
        audit.record("DECREASE_STOCK", "inventory", productId.value(),
                "Salida de stock: " + command.quantity());
        return inventory.decreaseStock(productId, variantId, command);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Inventory adjustStock(ProductId productId, ProductVariantId variantId, AdjustStockCommand command) {
        audit.record("ADJUST_STOCK", "inventory", productId.value(),
                "Ajuste de stock: " + command.targetQuantity());
        return inventory.adjustStock(productId, variantId, command);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public List<StockMovement> movementHistory(ProductId productId, ProductVariantId variantId) {
        return inventory.movementHistory(productId, variantId);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public boolean isLowStock(ProductId productId, ProductVariantId variantId) {
        return inventory.isLowStock(productId, variantId);
    }
}