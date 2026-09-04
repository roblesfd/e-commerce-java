package org.fernandodev.ecommerce.promotion.adapter.in.rest.mapper;


import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.ApplyCouponRequest;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.CouponApplicationResponse;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.CouponRequest;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.CouponResponse;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.CouponValidationResponse;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.UpdateCouponRequest;
import org.fernandodev.ecommerce.promotion.application.dto.ApplyCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.CartItemDto;
import org.fernandodev.ecommerce.promotion.application.dto.CouponValidationResult;
import org.fernandodev.ecommerce.promotion.application.dto.CreateCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdateCouponCommand;
import org.fernandodev.ecommerce.promotion.domain.Coupon;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponApplication;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre DTOs REST y objetos de aplicación/dominio de cupones.
 */
@Component
public class CouponMapper {

    public CreateCouponCommand toCreateCommand(CouponRequest request) {
        return new CreateCouponCommand(request.code(), PromotionId.of(request.promotionId()),
                request.validFrom(), request.validTo());
    }

    public UpdateCouponCommand toUpdateCommand(UpdateCouponRequest request) {
        return new UpdateCouponCommand(request.validFrom(), request.validTo());
    }

    public ApplyCouponCommand toApplyCommand(ApplyCouponRequest request) {
        return new ApplyCouponCommand(
                request.code(),
                request.items().stream()
                        .map(item -> new CartItemDto(
                                ProductId.of(item.productId()),
                                item.quantity(),
                                PromotionMapper.money(item.unitPrice())))
                        .toList());
    }

    public CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId().value(),
                coupon.getCode(),
                coupon.getPromotionId().value(),
                coupon.isActive(),
                coupon.getValidFrom(),
                coupon.getValidTo());
    }

    public CouponApplicationResponse toApplicationResponse(CouponApplication application) {
        return new CouponApplicationResponse(
                application.code(),
                application.promotionId().value(),
                toMoney(application.discount()));
    }

    public CouponValidationResponse toValidationResponse(CouponValidationResult result) {
        return new CouponValidationResponse(
                result.valid(),
                result.getReason().orElse(null),
                result.getDiscount().map(CouponMapper::toValidationMoney).orElse(null));
    }

    private static CouponApplicationResponse.MoneyResponse toMoney(Money money) {
        return new CouponApplicationResponse.MoneyResponse(money.amount(), money.currencyCode());
    }

    private static CouponValidationResponse.MoneyResponse toValidationMoney(Money money) {
        return new CouponValidationResponse.MoneyResponse(money.amount(), money.currencyCode());
    }
}
