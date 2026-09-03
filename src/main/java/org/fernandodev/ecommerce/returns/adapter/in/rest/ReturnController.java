package org.fernandodev.ecommerce.returns.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.CreateReturnRequest;
import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.InspectReturnRequest;
import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.RegisterReturnShipmentRequest;
import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.ReturnResponse;
import org.fernandodev.ecommerce.returns.adapter.in.rest.mapper.ReturnMapper;
import org.fernandodev.ecommerce.returns.application.port.in.ReturnUseCases;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
 * Controlador REST de solicitudes de devolución.
 */
@RestController
@RequestMapping("/api/v1/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnUseCases returnUseCases;
    private final ReturnMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnResponse create(@Valid @RequestBody CreateReturnRequest request) {
        return mapper.toResponse(returnUseCases.create(
                OrderId.of(request.orderId()), mapper.toCommand(request)));
    }

    @GetMapping("/{id}")
    public ReturnResponse get(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.getById(ReturnId.of(id)));
    }

    @GetMapping("/me")
    public List<ReturnResponse> mine() {
        return returnUseCases.listByCustomer(SecurityUtils.requiredUserId()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping
    public ReturnResponse getByOrder(@RequestParam UUID orderId) {
        return returnUseCases.getByOrder(OrderId.of(orderId))
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Devolución no encontrada para la orden: " + orderId));
    }

    @PostMapping("/{id}/approve")
    public ReturnResponse approve(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.approve(ReturnId.of(id)));
    }

    @PostMapping("/{id}/reject")
    public ReturnResponse reject(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.reject(ReturnId.of(id)));
    }

    @PostMapping("/{id}/cancel")
    public ReturnResponse cancel(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.cancel(ReturnId.of(id)));
    }

    @PostMapping("/{id}/return-shipment")
    public ReturnResponse registerReturnShipment(@PathVariable UUID id,
                                                 @Valid @RequestBody RegisterReturnShipmentRequest request) {
        return mapper.toResponse(returnUseCases.registerReturnShipment(ReturnId.of(id), request.trackingNumber()));
    }

    @PostMapping("/{id}/receive")
    public ReturnResponse receive(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.receive(ReturnId.of(id)));
    }

    @PostMapping("/{id}/inspect")
    public ReturnResponse inspect(@PathVariable UUID id, @Valid @RequestBody InspectReturnRequest request) {
        return mapper.toResponse(returnUseCases.inspect(ReturnId.of(id),
                request.condition(), request.notes(), request.passed()));
    }

    @PostMapping("/{id}/complete")
    public ReturnResponse complete(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.complete(ReturnId.of(id)));
    }

    @PostMapping("/{id}/retry-refund")
    public ReturnResponse retryRefund(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.retryRefund(ReturnId.of(id)));
    }
}