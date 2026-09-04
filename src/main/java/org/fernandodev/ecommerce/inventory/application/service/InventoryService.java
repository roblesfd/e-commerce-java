package org.fernandodev.ecommerce.inventory.application.service;

import java.time.Instant;
import java.util.List;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.application.dto.AdjustStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.DecreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.IncreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.ReserveStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.SetMinimumStockCommand;
import org.fernandodev.ecommerce.inventory.application.port.in.InventoryUseCases;
import org.fernandodev.ecommerce.inventory.application.port.out.InventoryRepository;
import org.fernandodev.ecommerce.inventory.domain.Inventory;
import org.fernandodev.ecommerce.inventory.domain.value_object.InventoryId;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockMovement;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservation;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservationId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación del contexto Inventory.
 * <p>
 * Las operaciones que mutan stock o reservas se ejecutan dentro de una
 * transacción que bloquea la fila de inventario (puerto de salida
 * {@code findByProductAndVariantForUpdate}), garantizando atomicidad bajo
 * concurrencia (spec §24). No depende de Spring salvo por la gestión de
 * transacciones.
 * </p>
 */
@RequiredArgsConstructor
public class InventoryService implements InventoryUseCases {

    private final InventoryRepository inventories;

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException si el inventario no está definido.
     */
    @Override
    public Inventory getByProduct(ProductId productId, ProductVariantId variantId) {
        return getExisting(productId, variantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int availableStock(ProductId productId, ProductVariantId variantId) {
        return inventories.findByProductAndVariant(productId, variantId)
                .map(Inventory::availableQuantity)
                .orElse(0);
    }

    /**
     * {@inheritDoc}
     *
     * @throws BusinessRuleViolationException si {@code quantity} no es positiva.
     */
    @Override
    @Transactional
    public Inventory increaseStock(ProductId productId, ProductVariantId variantId, IncreaseStockCommand command) {
        Inventory inventory = inventories.findByProductAndVariantForUpdate(productId, variantId)
                .orElseGet(() -> Inventory.define(InventoryId.generate(), productId, variantId, 0, 0));
        inventory.increase(command.quantity(), command.reason(), Instant.now());
        return inventories.save(inventory);
    }

    /**
     * {@inheritDoc}
     *
     * @throws BusinessRuleViolationException si no hay stock suficiente o se reduce por debajo de lo reservado.
     * @throws ResourceNotFoundException      si el inventario no está definido.
     */
    @Override
    @Transactional
    public Inventory decreaseStock(ProductId productId, ProductVariantId variantId, DecreaseStockCommand command) {
        Inventory inventory = lockExisting(productId, variantId);
        inventory.decrease(command.quantity(), command.reason(), Instant.now());
        return inventories.save(inventory);
    }

    /**
     * {@inheritDoc}
     *
     * @throws BusinessRuleViolationException si el objetivo es negativo o queda por debajo de lo reservado.
     * @throws ResourceNotFoundException      si el inventario no está definido.
     */
    @Override
    @Transactional
    public Inventory adjustStock(ProductId productId, ProductVariantId variantId, AdjustStockCommand command) {
        Inventory inventory = lockExisting(productId, variantId);
        inventory.adjust(command.targetQuantity(), command.reason(), Instant.now());
        return inventories.save(inventory);
    }

    /**
     * {@inheritDoc}
     *
     * @throws BusinessRuleViolationException si el umbral es negativo.
     * @throws ResourceNotFoundException      si el inventario no está definido.
     */
    @Override
    @Transactional
    public Inventory setMinimumStock(ProductId productId, ProductVariantId variantId, SetMinimumStockCommand command) {
        Inventory inventory = lockExisting(productId, variantId);
        inventory.setMinimumStock(command.minimumStock());
        return inventories.save(inventory);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException si el inventario no está definido.
     */
    @Override
    public List<StockMovement> movementHistory(ProductId productId, ProductVariantId variantId) {
        return getExisting(productId, variantId).getMovements();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLowStock(ProductId productId, ProductVariantId variantId) {
        return inventories.findByProductAndVariant(productId, variantId)
                .map(Inventory::isLowStock)
                .orElse(false);
    }

    /**
     * {@inheritDoc}
     *
     * @throws BusinessRuleViolationException si no hay stock suficiente.
     * @throws ResourceNotFoundException      si el inventario no está definido.
     */
    @Override
    @Transactional
    public StockReservation reserveStock(ProductId productId, ProductVariantId variantId, ReserveStockCommand command) {
        Inventory inventory = lockExisting(productId, variantId);
        StockReservation reservation = inventory.reserve(command.quantity(), command.expiresAt(), Instant.now());
        inventories.save(inventory);
        return reservation;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException   si el inventario o la reserva no existen.
     * @throws BusinessRuleViolationException si la reserva no está activa.
     */
    @Override
    @Transactional
    public Inventory confirmReservation(ProductId productId, ProductVariantId variantId,
                                        StockReservationId reservationId) {
        Inventory inventory = lockExisting(productId, variantId);
        inventory.confirmReservation(reservationId, Instant.now());
        return inventories.save(inventory);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException   si el inventario o la reserva no existen.
     * @throws BusinessRuleViolationException si la reserva no está activa.
     */
    @Override
    @Transactional
    public Inventory releaseReservation(ProductId productId, ProductVariantId variantId,
                                        StockReservationId reservationId) {
        Inventory inventory = lockExisting(productId, variantId);
        inventory.releaseReservation(reservationId, Instant.now());
        return inventories.save(inventory);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException   si el inventario o la reserva no existen.
     * @throws BusinessRuleViolationException si la reserva no está activa.
     */
    @Override
    @Transactional
    public Inventory expireReservation(ProductId productId, ProductVariantId variantId,
                                       StockReservationId reservationId) {
        Inventory inventory = lockExisting(productId, variantId);
        inventory.expireReservation(reservationId, Instant.now());
        return inventories.save(inventory);
    }

    /**
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @return el inventario existente.
     * @throws ResourceNotFoundException si no está definido.
     */
    private Inventory getExisting(ProductId productId, ProductVariantId variantId) {
        return inventories.findByProductAndVariant(productId, variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventario no definido para el producto: " + productId));
    }

    /**
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @return el inventario existente con la fila bloqueada para escritura.
     * @throws ResourceNotFoundException si no está definido.
     */
    private Inventory lockExisting(ProductId productId, ProductVariantId variantId) {
        return inventories.findByProductAndVariantForUpdate(productId, variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventario no definido para el producto: " + productId));
    }
}