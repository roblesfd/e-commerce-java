package org.fernandodev.ecommerce.admin.adapter.in.rest;

import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminPromotionUseCases;
import org.fernandodev.ecommerce.admin.security.AdminRoles;
import org.fernandodev.ecommerce.admin.security.RequireGroup;
import org.fernandodev.ecommerce.admin.security.RequireRole;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.application.dto.CreateCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.CreatePromotionCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdateCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdatePromotionCommand;
import org.fernandodev.ecommerce.promotion.domain.Coupon;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de administración de promociones y cupones.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final AdminPromotionUseCases adminPromotion;

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/promotions")
    public Promotion createPromotion(@Valid @RequestBody CreatePromotionCommand command) {
        return adminPromotion.createPromotion(command);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PutMapping("/promotions/{id}")
    public Promotion updatePromotion(@PathVariable UUID id, @Valid @RequestBody UpdatePromotionCommand command) {
        return adminPromotion.updatePromotion(PromotionId.of(id), command);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/promotions/{id}/activate")
    public Promotion activatePromotion(@PathVariable UUID id) {
        return adminPromotion.activatePromotion(PromotionId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/promotions/{id}/deactivate")
    public Promotion deactivatePromotion(@PathVariable UUID id) {
        return adminPromotion.deactivatePromotion(PromotionId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/promotions/{id}/products/{productId}")
    public Promotion addProductToPromotion(@PathVariable UUID id, @PathVariable UUID productId) {
        return adminPromotion.addProductToPromotion(PromotionId.of(id), ProductId.of(productId));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/coupons")
    public Coupon createCoupon(@Valid @RequestBody CreateCouponCommand command) {
        return adminPromotion.createCoupon(command);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PutMapping("/coupons/{id}")
    public Coupon updateCoupon(@PathVariable UUID id, @Valid @RequestBody UpdateCouponCommand command) {
        return adminPromotion.updateCoupon(CouponId.of(id), command);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/coupons/{id}/activate")
    public Coupon activateCoupon(@PathVariable UUID id) {
        return adminPromotion.activateCoupon(CouponId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/coupons/{id}/deactivate")
    public Coupon deactivateCoupon(@PathVariable UUID id) {
        return adminPromotion.deactivateCoupon(CouponId.of(id));
    }
}