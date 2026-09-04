package org.fernandodev.ecommerce.shipping.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.CreateShipmentRequest;
import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.ShipmentResponse;
import org.fernandodev.ecommerce.shipping.adapter.in.rest.mapper.ShipmentMapper;
import org.fernandodev.ecommerce.shipping.application.port.in.ShipmentUseCases;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentId;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de envíos.
 */
@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentUseCases shipmentUseCases;
    private final ShipmentMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse create(@Valid @RequestBody CreateShipmentRequest request) {
        return mapper.toResponse(shipmentUseCases.create(
                OrderId.of(request.orderId()), mapper.toCommand(request)));
    }

    @GetMapping("/{id}")
    /**
     * Get.
     * @param id id.
     */
    public ShipmentResponse get(@PathVariable UUID id) {
        return mapper.toResponse(shipmentUseCases.getById(ShipmentId.of(id)));
    }

    @GetMapping("/me")
    /**
     * Mine.
     */
    public List<ShipmentResponse> mine() {
        return shipmentUseCases.listByCustomer(SecurityUtils.requiredUserId()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping
    /**
     * Get by order.
     * @param orderId orderId.
     */
    public ShipmentResponse getByOrder(@RequestParam UUID orderId) {
        return shipmentUseCases.getByOrder(OrderId.of(orderId))
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado para la orden: " + orderId));
    }

    @PostMapping("/{id}/prepared")
    /**
     * Mark prepared.
     * @param id id.
     */
    public ShipmentResponse markPrepared(@PathVariable UUID id) {
        return mapper.toResponse(shipmentUseCases.markPrepared(ShipmentId.of(id)));
    }

    @PostMapping("/{id}/shipped")
    /**
     * Mark shipped.
     * @param id id.
     */
    public ShipmentResponse markShipped(@PathVariable UUID id) {
        return mapper.toResponse(shipmentUseCases.markShipped(ShipmentId.of(id)));
    }

    @PostMapping("/{id}/in-transit")
    /**
     * Mark in transit.
     * @param id id.
     */
    public ShipmentResponse markInTransit(@PathVariable UUID id) {
        return mapper.toResponse(shipmentUseCases.markInTransit(ShipmentId.of(id)));
    }

    @PostMapping("/{id}/out-for-delivery")
    /**
     * Mark out for delivery.
     * @param id id.
     */
    public ShipmentResponse markOutForDelivery(@PathVariable UUID id) {
        return mapper.toResponse(shipmentUseCases.markOutForDelivery(ShipmentId.of(id)));
    }

    @PostMapping("/{id}/delivered")
    /**
     * Mark delivered.
     * @param id id.
     */
    public ShipmentResponse markDelivered(@PathVariable UUID id) {
        return mapper.toResponse(shipmentUseCases.markDelivered(ShipmentId.of(id)));
    }

    @PostMapping("/{id}/failed")
    /**
     * Handle failure.
     * @param id id.
     */
    public ShipmentResponse handleFailure(@PathVariable UUID id) {
        return mapper.toResponse(shipmentUseCases.handleFailure(ShipmentId.of(id)));
    }

    @PostMapping("/{id}/cancel")
    /**
     * Cancel.
     * @param id id.
     */
    public ShipmentResponse cancel(@PathVariable UUID id) {
        return mapper.toResponse(shipmentUseCases.cancel(ShipmentId.of(id)));
    }
}