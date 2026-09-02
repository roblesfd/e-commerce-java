package org.fernandodev.ecommerce.promotion.adapter.util.mapper;

import org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa.entity.CouponJpaEntity;
import org.fernandodev.ecommerce.promotion.domain.Coupon;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;

/**
 * Mapper manual entre el agregado de dominio {@link Coupon} y la entidad JPA
 * {@link CouponJpaEntity}. Sin lógica de negocio.
 */
public final class CouponPersistenceMapper {

    private CouponPersistenceMapper() {
    }

    public static CouponJpaEntity toEntity(Coupon coupon) {
        CouponJpaEntity entity = new CouponJpaEntity();
        entity.setId(coupon.getId().value());
        entity.setCode(coupon.getCode());
        entity.setPromotionId(coupon.getPromotionId().value());
        entity.setActive(coupon.isActive());
        entity.setValidFrom(coupon.getValidFrom());
        entity.setValidTo(coupon.getValidTo());
        return entity;
    }

    public static Coupon toDomain(CouponJpaEntity entity) {
        return Coupon.crear(
                CouponId.of(entity.getId()),
                entity.getCode(),
                PromotionId.of(entity.getPromotionId()),
                entity.isActive(),
                entity.getValidFrom(),
                entity.getValidTo());
    }
}