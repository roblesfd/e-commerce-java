package org.fernandodev.ecommerce.inventory.adapter.in.rest;

import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.AdjustStockRequest;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.AvailableStockResponse;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.DecreaseStockRequest;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.IncreaseStockRequest;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.InventoryResponse;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.ReserveStockRequest;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.dto.SetMinimumStockRequest;
import org.fernandodev.ecommerce.inventory.adapter.in.rest.mapper.InventoryMapper;
import org.fernandodev.ecommerce.inventory.application.port.in.InventoryUseCases;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservationId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST del contexto Inventory.
 */
@RestController
@RequestMapping("/api/v1/products/{productId}/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryUseCases inventoryUseCases;
    private final InventoryMapper mapper;

    /**
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @return el inventario del producto/variante.
     */
    @GetMapping
    public InventoryResponse get(@PathVariable UUID productId,
                                 @RequestParam(required = false) UUID variantId) {
        return mapper.toResponse(inventoryUseCases.getByProduct(
                InventoryMapper.product(productId), variant(variantId)));
    }

    /**
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @return el stock disponible y si está por debajo del mínimo.
     */
    @GetMapping("/available")
    public AvailableStockResponse available(@PathVariable UUID productId,
                                            @RequestParam(required = false) UUID variantId) {
        return mapper.toAvailableResponse(inventoryUseCases.getByProduct(
                InventoryMapper.product(productId), variant(variantId)));
    }

    /**
     * Incrementa el stock del producto/variante.
     *
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @param request   datos del incremento.
     * @return el inventario actualizado.
     */
    @PostMapping("/stock/increase")
    public InventoryResponse increase(@PathVariable UUID productId,
                                      @RequestParam(required = false) UUID variantId,
                                      @Valid @RequestBody IncreaseStockRequest request) {
        return mapper.toResponse(inventoryUseCases.increaseStock(
                InventoryMapper.product(productId), variant(variantId), mapper.toCommand(request)));
    }

    /**
     * Disminuye el stock del producto/variante.
     *
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @param request   datos de la disminución.
     * @return el inventario actualizado.
     */
    @PostMapping("/stock/decrease")
    public InventoryResponse decrease(@PathVariable UUID productId,
                                      @RequestParam(required = false) UUID variantId,
                                      @Valid @RequestBody DecreaseStockRequest request) {
        return mapper.toResponse(inventoryUseCases.decreaseStock(
                InventoryMapper.product(productId), variant(variantId), mapper.toCommand(request)));
    }

    /**
     * Ajusta el stock a un valor objetivo.
     *
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @param request   datos del ajuste.
     * @return el inventario actualizado.
     */
    @PutMapping("/stock")
    public InventoryResponse adjust(@PathVariable UUID productId,
                                    @RequestParam(required = false) UUID variantId,
                                    @Valid @RequestBody AdjustStockRequest request) {
        return mapper.toResponse(inventoryUseCases.adjustStock(
                InventoryMapper.product(productId), variant(variantId), mapper.toCommand(request)));
    }

    /**
     * Define el umbral de stock mínimo.
     *
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @param request   datos del umbral.
     * @return el inventario actualizado.
     */
    @PatchMapping("/minimum-stock")
    public InventoryResponse setMinimumStock(@PathVariable UUID productId,
                                             @RequestParam(required = false) UUID variantId,
                                             @Valid @RequestBody SetMinimumStockRequest request) {
        return mapper.toResponse(inventoryUseCases.setMinimumStock(
                InventoryMapper.product(productId), variant(variantId), mapper.toCommand(request)));
    }

    /**
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @return el historial de movimientos del inventario.
     */
    @GetMapping("/movements")
    public java.util.List<InventoryResponse.StockMovementResponse> movements(
            @PathVariable UUID productId,
            @RequestParam(required = false) UUID variantId) {
        return inventoryUseCases.movementHistory(InventoryMapper.product(productId), variant(variantId)).stream()
                .map(m -> new InventoryResponse.StockMovementResponse(m.type().name(), m.quantity(), m.reason(), m.at()))
                .toList();
    }

    /**
     * Reserva stock de forma concurrente.
     *
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @param request   datos de la reserva.
     * @return la reserva creada.
     */
    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse.StockReservationResponse reserve(@PathVariable UUID productId,
                                                              @RequestParam(required = false) UUID variantId,
                                                              @Valid @RequestBody ReserveStockRequest request) {
        return mapper.toReservationResponse(inventoryUseCases.reserveStock(
                InventoryMapper.product(productId), variant(variantId), mapper.toCommand(request)));
    }

    /**
     * Confirma una reserva (descuenta el stock).
     *
     * @param productId    producto.
     * @param reservationId identificador de la reserva.
     * @param variantId    variante (null si el stock es a nivel producto).
     * @return el inventario actualizado.
     */
    @PostMapping("/reservations/{reservationId}/confirm")
    public InventoryResponse confirmReservation(@PathVariable UUID productId,
                                                @PathVariable UUID reservationId,
                                                @RequestParam(required = false) UUID variantId) {
        return mapper.toResponse(inventoryUseCases.confirmReservation(
                InventoryMapper.product(productId), variant(variantId), StockReservationId.of(reservationId)));
    }

    /**
     * Libera una reserva (devuelve el stock reservado).
     *
     * @param productId    producto.
     * @param reservationId identificador de la reserva.
     * @param variantId    variante (null si el stock es a nivel producto).
     * @return el inventario actualizado.
     */
    @PostMapping("/reservations/{reservationId}/release")
    public InventoryResponse releaseReservation(@PathVariable UUID productId,
                                                @PathVariable UUID reservationId,
                                                @RequestParam(required = false) UUID variantId) {
        return mapper.toResponse(inventoryUseCases.releaseReservation(
                InventoryMapper.product(productId), variant(variantId), StockReservationId.of(reservationId)));
    }

    /**
     * Expira una reserva (devuelve el stock reservado).
     *
     * @param productId    producto.
     * @param reservationId identificador de la reserva.
     * @param variantId    variante (null si el stock es a nivel producto).
     * @return el inventario actualizado.
     */
    @PostMapping("/reservations/{reservationId}/expire")
    public InventoryResponse expireReservation(@PathVariable UUID productId,
                                               @PathVariable UUID reservationId,
                                               @RequestParam(required = false) UUID variantId) {
        return mapper.toResponse(inventoryUseCases.expireReservation(
                InventoryMapper.product(productId), variant(variantId), StockReservationId.of(reservationId)));
    }

    /**
     * @param variantId variante en texto, o {@code null}.
     * @return la variante de dominio equivalente, o {@code null}.
     */
    private static ProductVariantId variant(UUID variantId) {
        return variantId == null ? null : ProductVariantId.of(variantId);
    }
}