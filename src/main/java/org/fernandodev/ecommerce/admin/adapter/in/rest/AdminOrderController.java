package org.fernandodev.ecommerce.admin.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminOrderUseCases;
import org.fernandodev.ecommerce.admin.security.AdminRoles;
import org.fernandodev.ecommerce.admin.security.RequireGroup;
import org.fernandodev.ecommerce.admin.security.RequireRole;
import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de administración de órdenes.
 * <p>
 * Permite consultar órdenes y avanzar su ciclo de vida (cancelar, marcar como
 * enviada o entregada). Todos los endpoints están protegidos y requieren rol
 * y grupo de administrador.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderUseCases adminOrder;

    /**
     * Lista todas las órdenes registradas.
     *
     * @return las órdenes existentes.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping
    public List<Order> list() {
        return adminOrder.listAllOrders();
    }

    /**
     * Consulta una orden por identificador.
     *
     * @param id identificador de la orden.
     * @return la orden encontrada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/{id}")
    public Order get(@PathVariable UUID id) {
        return adminOrder.getOrder(OrderId.of(id));
    }

    /**
     * Cancela una orden.
     *
     * @param id identificador de la orden.
     * @return la orden cancelada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/cancel")
    public Order cancel(@PathVariable UUID id) {
        return adminOrder.cancelOrder(OrderId.of(id));
    }

    /**
     * Marca una orden como enviada.
     *
     * @param id identificador de la orden.
     * @return la orden con el estado actualizado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/shipped")
    public Order shipped(@PathVariable UUID id) {
        return adminOrder.markOrderShipped(OrderId.of(id));
    }

    /**
     * Marca una orden como entregada.
     *
     * @param id identificador de la orden.
     * @return la orden con el estado actualizado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/delivered")
    public Order delivered(@PathVariable UUID id) {
        return adminOrder.markOrderDelivered(OrderId.of(id));
    }
}