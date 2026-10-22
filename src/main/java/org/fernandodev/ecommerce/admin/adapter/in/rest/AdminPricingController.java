package org.fernandodev.ecommerce.admin.adapter.in.rest;

import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminPricingUseCases;
import org.fernandodev.ecommerce.admin.security.AdminRoles;
import org.fernandodev.ecommerce.admin.security.RequireGroup;
import org.fernandodev.ecommerce.admin.security.RequireRole;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.application.dto.AddPriceOverrideCommand;
import org.fernandodev.ecommerce.pricing.application.dto.DefinePriceCommand;
import org.fernandodev.ecommerce.pricing.application.dto.UpdatePriceCommand;
import org.fernandodev.ecommerce.pricing.domain.Price;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceOverrideId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de administración de precios.
 * <p>
 * Permite definir y actualizar precios base, gestionar overrides y consultar
 * el precio de un producto. Todos los endpoints están protegidos y requieren
 * rol y grupo de administrador.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/prices")
@RequiredArgsConstructor
public class AdminPricingController {

    private final AdminPricingUseCases adminPricing;

    /**
     * Define el precio base de un producto.
     *
     * @param command datos del precio base.
     * @return el precio creado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping
    public Price defineBasePrice(@Valid @RequestBody DefinePriceCommand command) {
        return adminPricing.defineBasePrice(command);
    }

    /**
     * Actualiza un precio base existente.
     *
     * @param id      identificador del precio.
     * @param command datos actualizados del precio.
     * @return el precio actualizado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PutMapping("/{id}")
    public Price updateBasePrice(@PathVariable UUID id, @Valid @RequestBody UpdatePriceCommand command) {
        return adminPricing.updateBasePrice(PriceId.of(id), command);
    }

    /**
     * Añade un override de precio a un precio existente.
     *
     * @param id      identificador del precio.
     * @param command datos del override.
     * @return el precio con el override añadido.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/overrides")
    public Price addOverride(@PathVariable UUID id, @Valid @RequestBody AddPriceOverrideCommand command) {
        return adminPricing.addOverride(PriceId.of(id), command);
    }

    /**
     * Elimina un override de precio.
     *
     * @param id         identificador del precio.
     * @param overrideId identificador del override.
     * @return el precio con el override eliminado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/overrides/{overrideId}/remove")
    public Price removeOverride(@PathVariable UUID id, @PathVariable UUID overrideId) {
        return adminPricing.removeOverride(PriceId.of(id), PriceOverrideId.of(overrideId));
    }

    /**
     * Consulta el precio de un producto, opcionalmente por variante.
     *
     * @param productId identificador del producto.
     * @param variantId identificador de la variante (opcional).
     * @return el precio vigente del producto.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/by-product")
    public Price getByProduct(@RequestParam UUID productId, @RequestParam(required = false) UUID variantId) {
        return adminPricing.getPriceByProduct(ProductId.of(productId),
                variantId == null ? null : ProductVariantId.of(variantId));
    }
}