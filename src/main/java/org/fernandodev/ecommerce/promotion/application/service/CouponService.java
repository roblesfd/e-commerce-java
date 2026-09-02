package org.fernandodev.ecommerce.promotion.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.promotion.application.dto.ApplyCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.CartItemDto;
import org.fernandodev.ecommerce.promotion.application.dto.CouponValidationResult;
import org.fernandodev.ecommerce.promotion.application.dto.CreateCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdateCouponCommand;
import org.fernandodev.ecommerce.promotion.application.port.in.CouponUseCases;
import org.fernandodev.ecommerce.promotion.application.port.out.CouponRepository;
import org.fernandodev.ecommerce.promotion.application.port.out.ProductCategoryProvider;
import org.fernandodev.ecommerce.promotion.application.port.out.PromotionRepository;
import org.fernandodev.ecommerce.promotion.domain.Coupon;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.CartContext;
import org.fernandodev.ecommerce.promotion.domain.value_object.CartItemLine;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponApplication;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de cupones.
 * <p>
 * La validación/aplicación de un cupón enriquece los items con sus categorías
 * (contexto catalog) y delega la evaluación de elegibilidad y el cálculo del
 * descuento a las reglas de dominio de {@link Promotion}.
 * </p>
 */
@RequiredArgsConstructor
public class CouponService implements CouponUseCases {

    private final CouponRepository coupons;
    private final PromotionRepository promotions;
    private final ProductCategoryProvider categories;

    @Override
    public Coupon create(CreateCouponCommand command) {
        if (!promotions.existsById(command.promotionId())) {
            throw new ResourceNotFoundException("Promoción no encontrada: " + command.promotionId());
        }
        if (coupons.existsByCode(command.code())) {
            throw new ConflictException("Ya existe un cupón con el código: " + command.code());
        }
        Coupon coupon = Coupon.create(CouponId.generate(), command.code(), command.promotionId(),
                command.validFrom(), command.validTo());
        return coupons.save(coupon);
    }

    @Override
    public Coupon getById(CouponId id) {
        return getExisting(id);
    }

    @Override
    public List<Coupon> list() {
        return coupons.findAll();
    }

    @Override
    public Coupon update(CouponId id, UpdateCouponCommand command) {
        Coupon coupon = getExisting(id);
        coupon.setValidity(command.validFrom(), command.validTo());
        return coupons.save(coupon);
    }

    @Override
    public Coupon activate(CouponId id) {
        Coupon coupon = getExisting(id);
        coupon.activate();
        return coupons.save(coupon);
    }

    @Override
    public Coupon deactivate(CouponId id) {
        Coupon coupon = getExisting(id);
        coupon.deactivate();
        return coupons.save(coupon);
    }

    @Override
    public CouponValidationResult validateCoupon(String code, UUID customerId, ApplyCouponCommand command) {
        Coupon coupon = coupons.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado: " + code));
        if (!coupon.isActiveAt(Instant.now())) {
            return CouponValidationResult.invalid("El cupón no está activo o está fuera de su periodo");
        }
        Promotion promotion = promotions.findById(coupon.getPromotionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Promoción no encontrada: " + coupon.getPromotionId()));
        CartContext cart = toCartContext(command);
        return promotion.ineligibilityReason(cart, customerId, Instant.now())
                .<CouponValidationResult>map(CouponValidationResult::invalid)
                .orElseGet(() -> CouponValidationResult.valid(promotion.computeDiscount(cart, Instant.now())));
    }

    @Override
    public CouponApplication applyCoupon(String code, UUID customerId, ApplyCouponCommand command) {
        if (command.items().isEmpty()) {
            throw new BusinessRuleViolationException("El carrito no puede estar vacío");
        }
        CouponValidationResult result = validateCoupon(code, customerId, command);
        if (!result.valid()) {
            throw new BusinessRuleViolationException("El cupón no es aplicable: " + result.getReason().orElse(""));
        }
        Promotion promotion = promotions.findById(coupons.findByCode(code).orElseThrow().getPromotionId())
                .orElseThrow();
        return new CouponApplication(code, promotion.getId(), result.getDiscount().orElseThrow());
    }

    private Coupon getExisting(CouponId id) {
        return coupons.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado: " + id));
    }

    private CartContext toCartContext(ApplyCouponCommand command) {
        List<CartItemLine> lines = command.items().stream()
                .map(this::toCartItemLine)
                .toList();
        return new CartContext(lines);
    }

    private CartItemLine toCartItemLine(CartItemDto item) {
        java.util.Set<CategoryId> categoryIds = categories.categoriesOf(item.productId());
        return new CartItemLine(item.productId(), categoryIds, item.quantity(), item.unitPrice());
    }
}