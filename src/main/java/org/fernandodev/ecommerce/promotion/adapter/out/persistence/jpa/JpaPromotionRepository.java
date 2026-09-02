package org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa.entity.PromotionJpaEntity;
import org.fernandodev.ecommerce.promotion.adapter.util.mapper.PromotionPersistenceMapper;
import org.fernandodev.ecommerce.promotion.application.port.out.PromotionRepository;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link PromotionRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaPromotionRepository implements PromotionRepository {

    private final PromotionSpringDataRepository springData;

    @Override
    public Promotion save(Promotion promotion) {
        PromotionJpaEntity saved = springData.save(PromotionPersistenceMapper.toEntity(promotion));
        return PromotionPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Promotion> findById(PromotionId id) {
        return springData.findById(id.value()).map(PromotionPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Promotion> findByIdForUpdate(PromotionId id) {
        return springData.findByIdForUpdate(id.value()).map(PromotionPersistenceMapper::toDomain);
    }

    @Override
    public List<Promotion> findAll() {
        return springData.findAll().stream().map(PromotionPersistenceMapper::toDomain).toList();
    }

    @Override
    public boolean existsById(PromotionId id) {
        return springData.existsById(id.value());
    }
}