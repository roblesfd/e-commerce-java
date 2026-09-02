package org.fernandodev.ecommerce.promotion.application.port.in;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.promotion.application.dto.ApplyCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.CouponValidationResult;
import org.fernandodev.ecommerce.promotion.application.dto.CreateCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdateCouponCommand;
import org.fernandodev.ecommerce.promotion.domain.Coupon;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponApplication;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;

/**
 * Casos de uso de cupones (puerto de entrada).
 */
public interface CouponUseCases {

    Coupon create(CreateCouponCommand command);

    Coupon getById(CouponId id);

    List<Coupon> list();

    Coupon update(CouponId id, UpdateCouponCommand command);

    Coupon activate(CouponId id);

    Coupon deactivate(CouponId id);

    CouponValidationResult validateCoupon(String code, UUID customerId, ApplyCouponCommand command);

    CouponApplication applyCoupon(String code, UUID customerId, ApplyCouponCommand command);
}