package org.fernandodev.ecommerce.inventory.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.application.dto.AdjustStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.DecreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.IncreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.ReserveStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.SetMinimumStockCommand;
import org.fernandodev.ecommerce.inventory.domain.Inventory;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockMovement;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservation;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservationId;

/**
 * Casos de uso del contexto Inventory (puerto de entrada).
 */
public interface InventoryUseCases {

    Inventory getByProduct(ProductId productId, ProductVariantId variantId);

    int availableStock(ProductId productId, ProductVariantId variantId);

    Inventory increaseStock(ProductId productId, ProductVariantId variantId, IncreaseStockCommand command);

    Inventory decreaseStock(ProductId productId, ProductVariantId variantId, DecreaseStockCommand command);

    Inventory adjustStock(ProductId productId, ProductVariantId variantId, AdjustStockCommand command);

    Inventory setMinimumStock(ProductId productId, ProductVariantId variantId, SetMinimumStockCommand command);

    List<StockMovement> movementHistory(ProductId productId, ProductVariantId variantId);

    boolean isLowStock(ProductId productId, ProductVariantId variantId);

    StockReservation reserveStock(ProductId productId, ProductVariantId variantId, ReserveStockCommand command);

    Inventory confirmReservation(ProductId productId, ProductVariantId variantId, StockReservationId reservationId);

    Inventory releaseReservation(ProductId productId, ProductVariantId variantId, StockReservationId reservationId);

    Inventory expireReservation(ProductId productId, ProductVariantId variantId, StockReservationId reservationId);
}