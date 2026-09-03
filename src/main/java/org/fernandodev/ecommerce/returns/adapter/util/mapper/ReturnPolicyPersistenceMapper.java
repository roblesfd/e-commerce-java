package org.fernandodev.ecommerce.returns.adapter.util.mapper;

import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa.entity.ReturnPolicyJpaEntity;
import org.fernandodev.ecommerce.returns.domain.ReturnPolicy;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnPolicyId;

/**
 * Mapeo entre el agregado {@link ReturnPolicy} y su entidad JPA.
 */
public final class ReturnPolicyPersistenceMapper {

    private ReturnPolicyPersistenceMapper() {
    }

    public static ReturnPolicyJpaEntity toEntity(ReturnPolicy policy) {
        ReturnPolicyJpaEntity entity = new ReturnPolicyJpaEntity();
        entity.setId(policy.getId().value());
        entity.setReturnWindowDays(policy.getReturnWindowDays());
        entity.setActive(policy.isActive());
        entity.setReturnableProductIds(policy.getReturnableProductIds().stream()
                .map(ProductId::value).collect(java.util.stream.Collectors.toSet()));
        return entity;
    }

    public static ReturnPolicy toDomain(ReturnPolicyJpaEntity entity) {
        java.util.Set<ProductId> products = entity.getReturnableProductIds().stream()
                .map(ProductId::of)
                .collect(java.util.stream.Collectors.toSet());
        return ReturnPolicy.crear(ReturnPolicyId.of(entity.getId()), entity.getReturnWindowDays(),
                entity.isActive(), products);
    }
}