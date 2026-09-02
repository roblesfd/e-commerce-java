package org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa.entity.CouponJpaEntity;
import org.fernandodev.ecommerce.promotion.adapter.util.mapper.CouponPersistenceMapper;
import org.fernandodev.ecommerce.promotion.application.port.out.CouponRepository;
import org.fernandodev.ecommerce.promotion.domain.Coupon;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link CouponRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaCouponRepository implements CouponRepository {

    private final CouponSpringDataRepository springData;

    @Override
    public Coupon save(Coupon coupon) {
        CouponJpaEntity saved = springData.save(CouponPersistenceMapper.toEntity(coupon));
        return CouponPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Coupon> findById(CouponId id) {
        return springData.findById(id.value()).map(CouponPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return springData.findByCode(code).map(CouponPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return springData.existsByCode(code);
    }

    @Override
    public List<Coupon> findAll() {
        return springData.findAll().stream().map(CouponPersistenceMapper::toDomain).toList();
    }
}