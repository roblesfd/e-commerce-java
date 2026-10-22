package org.fernandodev.ecommerce.admin.application.service;

import org.fernandodev.ecommerce.admin.application.port.in.AdminPromotionUseCases;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.application.dto.CreateCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.CreatePromotionCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdateCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdatePromotionCommand;
import org.fernandodev.ecommerce.promotion.application.port.in.CouponUseCases;
import org.fernandodev.ecommerce.promotion.application.port.in.PromotionUseCases;
import org.fernandodev.ecommerce.promotion.domain.Coupon;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Orquestación administrativa de promociones y cupones.
 */
@RequiredArgsConstructor
public class AdminPromotionService implements AdminPromotionUseCases {

    private final PromotionUseCases promotions;
    private final CouponUseCases coupons;
    private final AuditService audit;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Promotion createPromotion(CreatePromotionCommand command) {
        return promotions.create(command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Promotion updatePromotion(PromotionId id, UpdatePromotionCommand command) {
        return promotions.update(id, command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Promotion activatePromotion(PromotionId id) {
        return promotions.activate(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Promotion deactivatePromotion(PromotionId id) {
        audit.record("DEACTIVATE_PROMOTION", "promotion", id.value(), "Promoción desactivada");
        return promotions.deactivate(id);
    }

    /**
     * {@inheritDoc}
     */

    @Override
    @Transactional
    public Promotion addProductToPromotion(PromotionId id, ProductId productId) {
        return promotions.addProduct(id, productId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Coupon createCoupon(CreateCouponCommand command) {
        return coupons.create(command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Coupon updateCoupon(CouponId id, UpdateCouponCommand command) {
        return coupons.update(id, command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Coupon activateCoupon(CouponId id) {
        return coupons.activate(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Coupon deactivateCoupon(CouponId id) {
        audit.record("DEACTIVATE_COUPON", "coupon", id.value(), "Cupón desactivado");
        return coupons.deactivate(id);
    }
}
