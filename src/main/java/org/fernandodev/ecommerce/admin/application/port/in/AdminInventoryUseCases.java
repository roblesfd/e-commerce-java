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

    Inventory getInventory(ProductId productId, ProductVariantId variantId);

    Inventory increaseStock(ProductId productId, ProductVariantId variantId, IncreaseStockCommand command);

    Inventory decreaseStock(ProductId productId, ProductVariantId variantId, DecreaseStockCommand command);

    Inventory adjustStock(ProductId productId, ProductVariantId variantId, AdjustStockCommand command);

    List<StockMovement> movementHistory(ProductId productId, ProductVariantId variantId);

    boolean isLowStock(ProductId productId, ProductVariantId variantId);
}