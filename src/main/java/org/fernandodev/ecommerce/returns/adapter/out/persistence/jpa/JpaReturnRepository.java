package org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa.entity.ReturnJpaEntity;
import org.fernandodev.ecommerce.returns.adapter.util.mapper.ReturnPersistenceMapper;
import org.fernandodev.ecommerce.returns.application.port.out.ReturnRepository;
import org.fernandodev.ecommerce.returns.domain.Return;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link ReturnRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaReturnRepository implements ReturnRepository {

    private final ReturnSpringDataRepository springData;

    @Override
    public Return save(Return aReturn) {
        ReturnJpaEntity saved = springData.save(ReturnPersistenceMapper.toEntity(aReturn));
        return ReturnPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Return> findById(ReturnId id) {
        return springData.findById(id.value()).map(ReturnPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Return> findByOrderId(OrderId orderId) {
        return springData.findByOrderId(orderId.value()).map(ReturnPersistenceMapper::toDomain);
    }

    @Override
    public List<Return> findByCustomerId(UUID customerId) {
        return springData.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(ReturnPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByOrderId(OrderId orderId) {
        return springData.existsByOrderId(orderId.value());
    }

    @Override
    public List<Return> findAll() {
        return springData.findAllByOrderByCreatedAtDesc().stream()
                .map(ReturnPersistenceMapper::toDomain)
                .toList();
    }
}