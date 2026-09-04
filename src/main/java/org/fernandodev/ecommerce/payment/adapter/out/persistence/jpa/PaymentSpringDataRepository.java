package org.fernandodev.ecommerce.payment.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.payment.adapter.out.persistence.jpa.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

/**
 * Repositorio Spring Data JPA del agregado {@link PaymentJpaEntity}.
 */
public interface PaymentSpringDataRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    /**
     * Consulta el pago de una orden.
     */
    Optional<PaymentJpaEntity> findByOrderId(UUID orderId);

    /**
     * Consulta un pago por la transacción del proveedor.
     */
    Optional<PaymentJpaEntity> findByProviderTransactionId(String transactionId);

    /**
     * Indica si ya existe un pago con esa clave de idempotencia.
     */
    boolean existsByRequestId(String requestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentJpaEntity p where p.id = :id")
    /**
     * Consulta un pago bloqueando la fila (PESSIMISTIC_WRITE) para transiciones concurrentes.
     */
    Optional<PaymentJpaEntity> findByIdForUpdate(@Param("id") UUID id);

    /**
     * Lista todos los pagos ordenados por fecha de creación descendente.
     */
    List<PaymentJpaEntity> findAllByOrderByCreatedAtDesc();
}