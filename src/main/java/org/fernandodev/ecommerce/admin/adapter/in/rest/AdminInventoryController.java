package org.fernandodev.ecommerce.admin.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminInventoryUseCases;
import org.fernandodev.ecommerce.admin.security.AdminRoles;
import org.fernandodev.ecommerce.admin.security.RequireGroup;
import org.fernandodev.ecommerce.admin.security.RequireRole;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.application.dto.AdjustStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.DecreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.IncreaseStockCommand;
import org.fernandodev.ecommerce.inventory.domain.Inventory;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockMovement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de administración de inventario.
 * <p>
 * Permite consultar el stock y el historial de movimientos de un producto
 * y realizar aumentos, disminuciones o ajustes de stock. Todos los endpoints
 * están protegidos y requieren rol y grupo de administrador.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final AdminInventoryUseCases adminInventory;

    /**
     * Consulta el inventario de un producto, opcionalmente por variante.
     *
     * @param productId identificador del producto.
     * @param variantId identificador de la variante (opcional).
     * @return el inventario encontrado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/{productId}")
    public Inventory get(@PathVariable UUID productId, @RequestParam(required = false) UUID variantId) {
        return adminInventory.getInventory(ProductId.of(productId),
                variantId == null ? null : ProductVariantId.of(variantId));
    }

    /**
     * Incrementa el stock de un producto, opcionalmente por variante.
     *
     * @param productId identificador del producto.
     * @param variantId identificador de la variante (opcional).
     * @param command   datos del movimiento de incremento.
     * @return el inventario actualizado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{productId}/increase")
    public Inventory increase(@PathVariable UUID productId, @RequestParam(required = false) UUID variantId,
                              @Valid @RequestBody IncreaseStockCommand command) {
        return adminInventory.increaseStock(ProductId.of(productId),
                variantId == null ? null : ProductVariantId.of(variantId), command);
    }

    /**
     * Disminuye el stock de un producto, opcionalmente por variante.
     *
     * @param productId identificador del producto.
     * @param variantId identificador de la variante (opcional).
     * @param command   datos del movimiento de disminución.
     * @return el inventario actualizado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{productId}/decrease")
    public Inventory decrease(@PathVariable UUID productId, @RequestParam(required = false) UUID variantId,
                              @Valid @RequestBody DecreaseStockCommand command) {
        return adminInventory.decreaseStock(ProductId.of(productId),
                variantId == null ? null : ProductVariantId.of(variantId), command);
    }

    /**
     * Ajusta el stock de un producto, opcionalmente por variante, a un valor absoluto.
     *
     * @param productId identificador del producto.
     * @param variantId identificador de la variante (opcional).
     * @param command   datos del ajuste de stock.
     * @return el inventario actualizado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{productId}/adjust")
    public Inventory adjust(@PathVariable UUID productId, @RequestParam(required = false) UUID variantId,
                            @Valid @RequestBody AdjustStockCommand command) {
        return adminInventory.adjustStock(ProductId.of(productId),
                variantId == null ? null : ProductVariantId.of(variantId), command);
    }

    /**
     * Consulta el historial de movimientos de stock de un producto.
     *
     * @param productId identificador del producto.
     * @param variantId identificador de la variante (opcional).
     * @return los movimientos de stock del producto.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/{productId}/movements")
    public List<StockMovement> movements(@PathVariable UUID productId,
                                         @RequestParam(required = false) UUID variantId) {
        return adminInventory.movementHistory(ProductId.of(productId),
                variantId == null ? null : ProductVariantId.of(variantId));
    }
}