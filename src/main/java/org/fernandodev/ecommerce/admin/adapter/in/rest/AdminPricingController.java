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
 */
@RestController
@RequestMapping("/api/v1/admin/prices")
@RequiredArgsConstructor
public class AdminPricingController {

    private final AdminPricingUseCases adminPricing;

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping
    public Price defineBasePrice(@Valid @RequestBody DefinePriceCommand command) {
        return adminPricing.defineBasePrice(command);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PutMapping("/{id}")
    public Price updateBasePrice(@PathVariable UUID id, @Valid @RequestBody UpdatePriceCommand command) {
        return adminPricing.updateBasePrice(PriceId.of(id), command);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/overrides")
    public Price addOverride(@PathVariable UUID id, @Valid @RequestBody AddPriceOverrideCommand command) {
        return adminPricing.addOverride(PriceId.of(id), command);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/overrides/{overrideId}/remove")
    public Price removeOverride(@PathVariable UUID id, @PathVariable UUID overrideId) {
        return adminPricing.removeOverride(PriceId.of(id), PriceOverrideId.of(overrideId));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/by-product")
    public Price getByProduct(@RequestParam UUID productId, @RequestParam(required = false) UUID variantId) {
        return adminPricing.getPriceByProduct(ProductId.of(productId),
                variantId == null ? null : ProductVariantId.of(variantId));
    }
}