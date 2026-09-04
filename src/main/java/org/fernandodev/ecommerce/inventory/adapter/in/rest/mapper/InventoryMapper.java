package org.fernandodev.ecommerce.inventory.adapter.in.rest.mapper;

import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.AdjustStockRequest;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.AvailableStockResponse;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.DecreaseStockRequest;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.IncreaseStockRequest;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.InventoryResponse;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.ReserveStockRequest;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.SetMinimumStockRequest;
import org.fernandodev.ecommerce.inventory.application.dto.AdjustStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.DecreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.IncreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.ReserveStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.SetMinimumStockCommand;
import org.fernandodev.ecommerce.inventory.domain.Inventory;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservation;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre DTOs REST y objetos de aplicación/dominio del contexto
 * Inventory. Sin lógica de negocio.
 */
@Component
public class InventoryMapper {

    /**
     * @param request petición de incremento.
     * @return comando de aplicación equivalente.
     */
    public IncreaseStockCommand toCommand(IncreaseStockRequest request) {
        return new IncreaseStockCommand(request.quantity(), request.reason());
    }

    /**
     * @param request petición de disminución.
     * @return comando de aplicación equivalente.
     */
    public DecreaseStockCommand toCommand(DecreaseStockRequest request) {
        return new DecreaseStockCommand(request.quantity(), request.reason());
    }

    /**
     * @param request petición de ajuste.
     * @return comando de aplicación equivalente.
     */
    public AdjustStockCommand toCommand(AdjustStockRequest request) {
        return new AdjustStockCommand(request.targetQuantity(), request.reason());
    }

    /**
     * @param request petición de umbral mínimo.
     * @return comando de aplicación equivalente.
     */
    public SetMinimumStockCommand toCommand(SetMinimumStockRequest request) {
        return new SetMinimumStockCommand(request.minimumStock());
    }

    /**
     * @param request petición de reserva.
     * @return comando de aplicación equivalente.
     */
    public ReserveStockCommand toCommand(ReserveStockRequest request) {
        return new ReserveStockCommand(request.quantity(), request.expiresAt());
    }

    /**
     * @param inventory agregado de dominio.
     * @return respuesta REST con el detalle del inventario.
     */
    public InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId().value(),
                inventory.getProductId().value(),
                inventory.getVariantId().map(ProductVariantId::value).orElse(null),
                inventory.getOnHand(),
                inventory.getReserved(),
                inventory.availableQuantity(),
                inventory.getMinimumStock(),
                inventory.isLowStock(),
                inventory.getMovements().stream()
                        .map(m -> new InventoryResponse.StockMovementResponse(
                                m.type().name(), m.quantity(), m.reason(), m.at()))
                        .toList(),
                inventory.getReservations().stream()
                        .map(this::toReservationResponse)
                        .toList());
    }

    /**
     * @param reservation reserva de dominio.
     * @return respuesta REST con la reserva.
     */
    public InventoryResponse.StockReservationResponse toReservationResponse(StockReservation reservation) {
        return new InventoryResponse.StockReservationResponse(
                reservation.id().value(),
                reservation.quantity(),
                reservation.status().name(),
                reservation.createdAt(),
                reservation.expiresAt());
    }

    /**
     * @param inventory agregado de dominio.
     * @return respuesta REST con el stock disponible y estado bajo mínimo.
     */
    public AvailableStockResponse toAvailableResponse(Inventory inventory) {
        return new AvailableStockResponse(inventory.availableQuantity(), inventory.isLowStock());
    }

    /**
     * @param variantId variante en texto, o {@code null}.
     * @return la variante de dominio equivalente, o {@code null}.
     */
    public static ProductVariantId variant(UUID variantId) {
        return variantId == null ? null : ProductVariantId.of(variantId);
    }

    /**
     * @param productId producto en texto.
     * @return el producto de dominio equivalente.
     */
    public static ProductId product(UUID productId) {
        return ProductId.of(productId);
    }
}