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

    /**
     * Crea una promoción.
     */
    Promotion create(CreatePromotionCommand command);

    /**
     * Consulta una promoción por identificador.
     */
    Promotion getById(PromotionId id);

    /**
     * Lista todas las promociones.
     */
    List<Promotion> list();

    /**
     * Actualiza una promoción.
     */
    Promotion update(PromotionId id, UpdatePromotionCommand command);

    /**
     * Activa una promoción.
     */
    Promotion activate(PromotionId id);

    /**
     * Desactiva una promoción.
     */
    Promotion deactivate(PromotionId id);

    /**
     * Añade un producto al alcance de la promoción.
     */
    Promotion addProduct(PromotionId id, ProductId productId);

    /**
     * Elimina un producto del alcance de la promoción.
     */
    Promotion removeProduct(PromotionId id, ProductId productId);

    Promotion addCategory(PromotionId id, CategoryId categoryId);

    Promotion removeCategory(PromotionId id, CategoryId categoryId);

    /**
     * Registra un uso de la promoción (llamado al confirmar la orden).
     */
    Promotion recordUsage(PromotionId id, UUID customerId);
}