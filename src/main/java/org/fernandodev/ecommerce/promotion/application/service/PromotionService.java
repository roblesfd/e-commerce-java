package org.fernandodev.ecommerce.promotion.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.application.dto.CreatePromotionCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdatePromotionCommand;
import org.fernandodev.ecommerce.promotion.application.port.in.PromotionUseCases;
import org.fernandodev.ecommerce.promotion.application.port.out.PromotionRepository;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de promociones.
 */
@RequiredArgsConstructor
public class PromotionService implements PromotionUseCases {

    private final PromotionRepository promotions;

    @Override
    public Promotion create(CreatePromotionCommand command) {
        Promotion promotion = Promotion.create(
                PromotionId.generate(),
                command.name(),
                command.type(),
                command.validFrom(),
                command.validTo(),
                command.percentageValue(),
                command.fixedAmount(),
                command.volumeTiers(),
                command.buyQuantity(),
                command.getQuantity(),
                command.minPurchaseAmount(),
                command.maxDiscount(),
                command.maxUsage(),
                command.maxUsagePerCustomer(),
                command.productIds(),
                command.categoryIds());
        return promotions.save(promotion);
    }

    @Override
    public Promotion getById(PromotionId id) {
        return getExisting(id);
    }

    @Override
    public List<Promotion> list() {
        return promotions.findAll();
    }

    @Override
    public Promotion update(PromotionId id, UpdatePromotionCommand command) {
        Promotion promotion = getExisting(id);
        promotion.updateName(command.name());
        promotion.setValidity(command.validFrom(), command.validTo());
        promotion.setLimits(command.minPurchaseAmount(), command.maxDiscount(),
                command.maxUsage(), command.maxUsagePerCustomer());
        replaceScope(promotion, command.productIds(), command.categoryIds());
        return promotions.save(promotion);
    }

    @Override
    public Promotion activate(PromotionId id) {
        Promotion promotion = getExisting(id);
        promotion.activate();
        return promotions.save(promotion);
    }

    @Override
    public Promotion deactivate(PromotionId id) {
        Promotion promotion = getExisting(id);
        promotion.deactivate();
        return promotions.save(promotion);
    }

    @Override
    public Promotion addProduct(PromotionId id, ProductId productId) {
        Promotion promotion = getExisting(id);
        promotion.addProduct(productId);
        return promotions.save(promotion);
    }

    @Override
    public Promotion removeProduct(PromotionId id, ProductId productId) {
        Promotion promotion = getExisting(id);
        promotion.removeProduct(productId);
        return promotions.save(promotion);
    }

    @Override
    public Promotion addCategory(PromotionId id, CategoryId categoryId) {
        Promotion promotion = getExisting(id);
        promotion.addCategory(categoryId);
        return promotions.save(promotion);
    }

    @Override
    public Promotion removeCategory(PromotionId id, CategoryId categoryId) {
        Promotion promotion = getExisting(id);
        promotion.removeCategory(categoryId);
        return promotions.save(promotion);
    }

    @Override
    @Transactional
    public Promotion recordUsage(PromotionId id, UUID customerId) {
        Promotion promotion = promotions.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada: " + id));
        promotion.recordUsage(customerId);
        return promotions.save(promotion);
    }

    private Promotion getExisting(PromotionId id) {
        return promotions.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada: " + id));
    }

    private void replaceScope(Promotion promotion, java.util.Set<ProductId> productIds,
                              java.util.Set<CategoryId> categoryIds) {
        promotion.getProductIds().forEach(promotion::removeProduct);
        productIds.forEach(promotion::addProduct);
        promotion.getCategoryIds().forEach(promotion::removeCategory);
        categoryIds.forEach(promotion::addCategory);
    }
}