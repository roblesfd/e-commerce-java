package org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa.entity.ReturnPolicyJpaEntity;
import org.fernandodev.ecommerce.returns.adapter.util.mapper.ReturnPolicyPersistenceMapper;
import org.fernandodev.ecommerce.returns.application.port.out.ReturnPolicyRepository;
import org.fernandodev.ecommerce.returns.domain.ReturnPolicy;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnPolicyId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link ReturnPolicyRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaReturnPolicyRepository implements ReturnPolicyRepository {

    private final ReturnPolicySpringDataRepository springData;

    @Override
    public ReturnPolicy save(ReturnPolicy policy) {
        ReturnPolicyJpaEntity saved = springData.save(ReturnPolicyPersistenceMapper.toEntity(policy));
        return ReturnPolicyPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<ReturnPolicy> findById(ReturnPolicyId id) {
        return springData.findById(id.value()).map(ReturnPolicyPersistenceMapper::toDomain);
    }

    @Override
    public Optional<ReturnPolicy> findActive() {
        return springData.findFirstByActiveTrue().map(ReturnPolicyPersistenceMapper::toDomain);
    }

    @Override
    public List<ReturnPolicy> findAll() {
        return springData.findAll().stream().map(ReturnPolicyPersistenceMapper::toDomain).toList();
    }
}