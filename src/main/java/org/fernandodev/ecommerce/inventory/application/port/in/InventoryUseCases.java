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

    /**
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @return el inventario del producto/variante.
     * @throws org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException
     *         si no está definido.
     */
    Inventory getByProduct(ProductId productId, ProductVariantId variantId);

    /**
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @return el stock disponible (o {@code 0} si no hay inventario definido).
     */
    int availableStock(ProductId productId, ProductVariantId variantId);

    /**
     * Incrementa el stock del producto/variante.
     *
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @param command   datos del incremento.
     * @return el inventario actualizado.
     */
    Inventory increaseStock(ProductId productId, ProductVariantId variantId, IncreaseStockCommand command);

    /**
     * Disminuye el stock del producto/variante.
     *
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @param command   datos de la disminución.
     * @return el inventario actualizado.
     */
    Inventory decreaseStock(ProductId productId, ProductVariantId variantId, DecreaseStockCommand command);

    /**
     * Ajusta el stock a un valor objetivo.
     *
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @param command   datos del ajuste.
     * @return el inventario actualizado.
     */
    Inventory adjustStock(ProductId productId, ProductVariantId variantId, AdjustStockCommand command);

    /**
     * Define el umbral de stock mínimo.
     *
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @param command   datos del umbral.
     * @return el inventario actualizado.
     */
    Inventory setMinimumStock(ProductId productId, ProductVariantId variantId, SetMinimumStockCommand command);

    /**
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @return el historial de movimientos del inventario.
     */
    List<StockMovement> movementHistory(ProductId productId, ProductVariantId variantId);

    /**
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @return {@code true} si el stock disponible está en o por debajo del mínimo.
     */
    boolean isLowStock(ProductId productId, ProductVariantId variantId);

    /**
     * Reserva stock de forma concurrente (PESSIMISTIC_WRITE), previniendo overselling.
     *
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @param command   datos de la reserva.
     * @return la reserva creada.
     */
    StockReservation reserveStock(ProductId productId, ProductVariantId variantId, ReserveStockCommand command);

    /**
     * Confirma una reserva (descuenta el stock).
     *
     * @param productId    producto.
     * @param variantId    variante (null si el stock es a nivel producto).
     * @param reservationId identificador de la reserva.
     * @return el inventario actualizado.
     */
    Inventory confirmReservation(ProductId productId, ProductVariantId variantId, StockReservationId reservationId);

    /**
     * Libera una reserva (devuelve el stock reservado).
     *
     * @param productId    producto.
     * @param variantId    variante (null si el stock es a nivel producto).
     * @param reservationId identificador de la reserva.
     * @return el inventario actualizado.
     */
    Inventory releaseReservation(ProductId productId, ProductVariantId variantId, StockReservationId reservationId);

    /**
     * Expira una reserva (devuelve el stock reservado).
     *
     * @param productId    producto.
     * @param variantId    variante (null si el stock es a nivel producto).
     * @param reservationId identificador de la reserva.
     * @return el inventario actualizado.
     */
    Inventory expireReservation(ProductId productId, ProductVariantId variantId, StockReservationId reservationId);
}