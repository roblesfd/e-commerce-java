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

/**
     * Crea una promoción.
     */
    Promotion createPromotion(CreatePromotionCommand command);

/**
     * Actualiza una promoción.
     */
    Promotion updatePromotion(PromotionId id, UpdatePromotionCommand command);

/**
     * Activa una promoción.
     */
    Promotion activatePromotion(PromotionId id);

/**
     * Desactiva una promoción y lo registra en auditoría.
     */
    Promotion deactivatePromotion(PromotionId id);

/**
     * Añade un producto al alcance de la promoción.
     */
    Promotion addProductToPromotion(PromotionId id, ProductId productId);

/**
     * Crea un cupón.
     */
    Coupon createCoupon(CreateCouponCommand command);

/**
     * Actualiza un cupón.
     */
    Coupon updateCoupon(CouponId id, UpdateCouponCommand command);

/**
     * Activa un cupón.
     */
    Coupon activateCoupon(CouponId id);

/**
     * Desactiva un cupón y lo registra en auditoría.
     */
    Coupon deactivateCoupon(CouponId id);
}