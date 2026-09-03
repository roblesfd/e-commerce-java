package org.fernandodev.ecommerce.shipping.adapter.util.mapper;

import org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa.entity.ShippingMethodJpaEntity;
import org.fernandodev.ecommerce.shipping.domain.ShippingMethod;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Mapper manual entre el agregado de dominio {@link ShippingMethod} y la
 * entidad JPA {@link ShippingMethodJpaEntity}. Sin lógica de negocio.
 */
public final class ShippingMethodPersistenceMapper {

    private ShippingMethodPersistenceMapper() {
    }

    public static ShippingMethodJpaEntity toEntity(ShippingMethod method) {
        ShippingMethodJpaEntity entity = new ShippingMethodJpaEntity();
        entity.setId(method.getId().value());
        entity.setName(method.getName());
        entity.setCarrier(method.getCarrier());
        entity.setCostAmount(method.getCost().amount());
        entity.setCurrency(method.getCost().currencyCode());
        entity.setActive(method.isActive());
        return entity;
    }

    public static ShippingMethod toDomain(ShippingMethodJpaEntity entity) {
        return ShippingMethod.crear(
                ShippingMethodId.of(entity.getId()),
                entity.getName(),
                entity.getCarrier(),
                Money.of(entity.getCostAmount(), entity.getCurrency()),
                entity.isActive());
    }
}