package org.fernandodev.ecommerce.promotion.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;

/**
 * Puerto de salida de persistencia del agregado {@link Promotion}.
 */
public interface PromotionRepository {

    Promotion save(Promotion promotion);

    Optional<Promotion> findById(PromotionId id);

    /**
     * Busca la promoción bloqueando la fila (PESSIMISTIC_WRITE) para el registro
     * de uso concurrente.
     */
    Optional<Promotion> findByIdForUpdate(PromotionId id);

    List<Promotion> findAll();

    boolean existsById(PromotionId id);
}