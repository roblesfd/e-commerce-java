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

    /**
     * Crea un cupón.
     */
    Coupon create(CreateCouponCommand command);

    /**
     * Consulta un cupón por identificador.
     */
    Coupon getById(CouponId id);

    /**
     * Lista todos los cupones.
     */
    List<Coupon> list();

    /**
     * Actualiza un cupón.
     */
    Coupon update(CouponId id, UpdateCouponCommand command);

    /**
     * Activa un cupón.
     */
    Coupon activate(CouponId id);

    /**
     * Desactiva un cupón.
     */
    Coupon deactivate(CouponId id);

    /**
     * Valida si un cupón es aplicable al cliente.
     */
    CouponValidationResult validateCoupon(String code, UUID customerId, ApplyCouponCommand command);

    /**
     * Aplica un cupón y devuelve el descuento.
     */
    CouponApplication applyCoupon(String code, UUID customerId, ApplyCouponCommand command);
}