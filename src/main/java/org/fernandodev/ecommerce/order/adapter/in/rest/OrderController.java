package org.fernandodev.ecommerce.order.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.order.adapter.in.rest.dto.OrderResponse;
import org.fernandodev.ecommerce.order.adapter.in.rest.mapper.OrderMapper;
import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST del contexto Order Management.
 * <p>
 * La creación de órdenes ocurre vía el flujo de Checkout (puerto de salida);
 * aquí se exponen consulta, confirmación y cancelación.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCases orderUseCases;
    private final OrderMapper mapper;

    /**
     * @return las órdenes del usuario autenticado.
     */
    @GetMapping("/me")
    public List<OrderResponse> mine() {
        return orderUseCases.listByCustomer(SecurityUtils.requiredUserId()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * @param id identificador de la orden.
     * @return el detalle de la orden.
     */
    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return mapper.toResponse(orderUseCases.getById(OrderId.of(id)));
    }

    /**
     * Confirma la orden.
     *
     * @param id identificador de la orden.
     * @return la orden confirmada.
     */
    @PatchMapping("/{id}/confirm")
    public OrderResponse confirm(@PathVariable UUID id) {
        return mapper.toResponse(orderUseCases.confirm(OrderId.of(id)));
    }

    /**
     * Cancela la orden.
     *
     * @param id identificador de la orden.
     * @return la orden cancelada.
     */
    @PatchMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable UUID id) {
        return mapper.toResponse(orderUseCases.cancel(OrderId.of(id)));
    }
}