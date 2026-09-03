package org.fernandodev.ecommerce.admin.application.port.in;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.application.dto.CreateCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.CreatePromotionCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdateCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdatePromotionCommand;
import org.fernandodev.ecommerce.promotion.domain.Coupon;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;

/**
 * Orquestación administrativa de promociones y cupones.
 */
public interface AdminPromotionUseCases {

    Promotion createPromotion(CreatePromotionCommand command);

    Promotion updatePromotion(PromotionId id, UpdatePromotionCommand command);

    Promotion activatePromotion(PromotionId id);

    Promotion deactivatePromotion(PromotionId id);

    Promotion addProductToPromotion(PromotionId id, ProductId productId);

    Coupon createCoupon(CreateCouponCommand command);

    Coupon updateCoupon(CouponId id, UpdateCouponCommand command);

    Coupon activateCoupon(CouponId id);

    Coupon deactivateCoupon(CouponId id);
}