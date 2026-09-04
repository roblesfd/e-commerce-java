package org.fernandodev.ecommerce.admin.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.application.dto.AdjustStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.DecreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.IncreaseStockCommand;
import org.fernandodev.ecommerce.inventory.domain.Inventory;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockMovement;

/**
 * Orquestación administrativa de inventario.
 */
public interface AdminInventoryUseCases {

/**
     * Consulta el inventario de un producto/variante.
     */
    Inventory getInventory(ProductId productId, ProductVariantId variantId);

/**
     * Incrementa el stock y lo registra en auditoría.
     */
    Inventory increaseStock(ProductId productId, ProductVariantId variantId, IncreaseStockCommand command);

/**
     * Disminuye el stock y lo registra en auditoría.
     */
    Inventory decreaseStock(ProductId productId, ProductVariantId variantId, DecreaseStockCommand command);

/**
     * Ajusta el stock a un objetivo y lo registra en auditoría.
     */
    Inventory adjustStock(ProductId productId, ProductVariantId variantId, AdjustStockCommand command);

/**
     * Consulta el historial de movimientos.
     */
    List<StockMovement> movementHistory(ProductId productId, ProductVariantId variantId);

/**
     * Indica si el stock está por debajo del mínimo.
     */
    boolean isLowStock(ProductId productId, ProductVariantId variantId);
}