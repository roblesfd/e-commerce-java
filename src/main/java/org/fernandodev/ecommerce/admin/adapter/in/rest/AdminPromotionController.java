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
 * <p>
 * Permite gestionar el ciclo de vida de promociones y cupones (crear,
 * actualizar, activar/desactivar) y asociar productos a una promoción.
 * Todos los endpoints están protegidos y requieren rol y grupo de administrador.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final AdminPromotionUseCases adminPromotion;

    /**
     * Crea una promoción.
     *
     * @param command datos de la promoción.
     * @return la promoción creada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/promotions")
    public Promotion createPromotion(@Valid @RequestBody CreatePromotionCommand command) {
        return adminPromotion.createPromotion(command);
    }

    /**
     * Actualiza una promoción existente.
     *
     * @param id      identificador de la promoción.
     * @param command datos actualizados de la promoción.
     * @return la promoción actualizada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PutMapping("/promotions/{id}")
    public Promotion updatePromotion(@PathVariable UUID id, @Valid @RequestBody UpdatePromotionCommand command) {
        return adminPromotion.updatePromotion(PromotionId.of(id), command);
    }

    /**
     * Activa una promoción.
     *
     * @param id identificador de la promoción.
     * @return la promoción activada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/promotions/{id}/activate")
    public Promotion activatePromotion(@PathVariable UUID id) {
        return adminPromotion.activatePromotion(PromotionId.of(id));
    }

    /**
     * Desactiva una promoción.
     *
     * @param id identificador de la promoción.
     * @return la promoción desactivada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/promotions/{id}/deactivate")
    public Promotion deactivatePromotion(@PathVariable UUID id) {
        return adminPromotion.deactivatePromotion(PromotionId.of(id));
    }

    /**
     * Asocia un producto a una promoción.
     *
     * @param id        identificador de la promoción.
     * @param productId identificador del producto.
     * @return la promoción con el producto asociado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/promotions/{id}/products/{productId}")
    public Promotion addProductToPromotion(@PathVariable UUID id, @PathVariable UUID productId) {
        return adminPromotion.addProductToPromotion(PromotionId.of(id), ProductId.of(productId));
    }

    /**
     * Crea un cupón de descuento.
     *
     * @param command datos del cupón.
     * @return el cupón creado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/coupons")
    public Coupon createCoupon(@Valid @RequestBody CreateCouponCommand command) {
        return adminPromotion.createCoupon(command);
    }

    /**
     * Actualiza un cupón existente.
     *
     * @param id      identificador del cupón.
     * @param command datos actualizados del cupón.
     * @return el cupón actualizado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PutMapping("/coupons/{id}")
    public Coupon updateCoupon(@PathVariable UUID id, @Valid @RequestBody UpdateCouponCommand command) {
        return adminPromotion.updateCoupon(CouponId.of(id), command);
    }

    /**
     * Activa un cupón.
     *
     * @param id identificador del cupón.
     * @return el cupón activado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/coupons/{id}/activate")
    public Coupon activateCoupon(@PathVariable UUID id) {
        return adminPromotion.activateCoupon(CouponId.of(id));
    }

    /**
     * Desactiva un cupón.
     *
     * @param id identificador del cupón.
     * @return el cupón desactivado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/coupons/{id}/deactivate")
    public Coupon deactivateCoupon(@PathVariable UUID id) {
        return adminPromotion.deactivateCoupon(CouponId.of(id));
    }
}