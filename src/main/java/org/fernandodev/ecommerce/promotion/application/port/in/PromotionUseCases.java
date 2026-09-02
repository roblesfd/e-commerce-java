package org.fernandodev.ecommerce.promotion.application.port.in;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.application.dto.CreatePromotionCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdatePromotionCommand;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;

/**
 * Casos de uso de promociones (puerto de entrada).
 */
public interface PromotionUseCases {

    Promotion create(CreatePromotionCommand command);

    Promotion getById(PromotionId id);

    List<Promotion> list();

    Promotion update(PromotionId id, UpdatePromotionCommand command);

    Promotion activate(PromotionId id);

    Promotion deactivate(PromotionId id);

    Promotion addProduct(PromotionId id, ProductId productId);

    Promotion removeProduct(PromotionId id, ProductId productId);

    Promotion addCategory(PromotionId id, CategoryId categoryId);

    Promotion removeCategory(PromotionId id, CategoryId categoryId);

    /**
     * Registra un uso de la promoción (llamado al confirmar la orden).
     */
    Promotion recordUsage(PromotionId id, UUID customerId);
}