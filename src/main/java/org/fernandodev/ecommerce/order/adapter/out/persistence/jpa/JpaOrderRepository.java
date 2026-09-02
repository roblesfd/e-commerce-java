package org.fernandodev.ecommerce.order.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.order.adapter.out.persistence.jpa.entity.OrderJpaEntity;
import org.fernandodev.ecommerce.order.adapter.util.mapper.OrderPersistenceMapper;
import org.fernandodev.ecommerce.order.application.port.out.OrderRepository;
import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link OrderRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaOrderRepository implements OrderRepository {

    private final OrderSpringDataRepository springData;

    @Override
    public Order save(Order order) {
        OrderJpaEntity saved = springData.save(OrderPersistenceMapper.toEntity(order));
        return OrderPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return springData.findById(id.value()).map(OrderPersistenceMapper::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(UUID customerId) {
        return springData.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(OrderPersistenceMapper::toDomain)
                .toList();
    }
}