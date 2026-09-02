package org.fernandodev.ecommerce.promotion.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.promotion.domain.Coupon;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;

/**
 * Puerto de salida de persistencia del agregado {@link Coupon}.
 */
public interface CouponRepository {

    Coupon save(Coupon coupon);

    Optional<Coupon> findById(CouponId id);

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    List<Coupon> findAll();
}