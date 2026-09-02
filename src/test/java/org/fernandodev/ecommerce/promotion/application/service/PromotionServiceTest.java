package org.fernandodev.ecommerce.promotion.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.application.dto.CreatePromotionCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdatePromotionCommand;
import org.fernandodev.ecommerce.promotion.application.port.out.PromotionRepository;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.DiscountType;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class PromotionServiceTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2099-12-31T00:00:00Z");

    private final FakePromotionRepository repository = new FakePromotionRepository();
    private final PromotionService service = new PromotionService(repository);

    @Test
    void createGuardaPromocion() {
        Promotion promotion = service.create(comando());

        assertThat(promotion.getId()).isNotNull();
        assertThat(promotion.isActive()).isTrue();
        assertThat(repository.findById(promotion.getId())).contains(promotion);
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(PromotionId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCambiaNombreValidezYScope() {
        Promotion promotion = service.create(comando());
        ProductId productId = ProductId.generate();

        Promotion updated = service.update(promotion.getId(),
                new UpdatePromotionCommand("Promo nueva", T1, T2, null, null, 5, 1,
                        Set.of(productId), Set.of()));

        assertThat(updated.getName()).isEqualTo("Promo nueva");
        assertThat(updated.getMaxUsage()).isEqualTo(5);
        assertThat(updated.getProductIds()).contains(productId);
    }

    @Test
    void activateYDeactivateCambianElEstado() {
        Promotion promotion = service.create(comando());

        Promotion desactivada = service.deactivate(promotion.getId());
        assertThat(desactivada.isActive()).isFalse();

        Promotion activada = service.activate(promotion.getId());
        assertThat(activada.isActive()).isTrue();
    }

    @Test
    void addProductAñadeAlScope() {
        Promotion promotion = service.create(comando());
        ProductId productId = ProductId.generate();

        Promotion updated = service.addProduct(promotion.getId(), productId);

        assertThat(updated.getProductIds()).contains(productId);
    }

    @Test
    void recordUsageIncrementaElUso() {
        Promotion promotion = service.create(comando());

        Promotion updated = service.recordUsage(promotion.getId(), UUID.randomUUID());

        assertThat(updated.getUsedCount()).isEqualTo(1);
    }

    @Test
    void recordUsageInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.recordUsage(PromotionId.generate(), UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static CreatePromotionCommand comando() {
        return new CreatePromotionCommand("Promo 10%", DiscountType.PERCENTAGE, new BigDecimal("10"),
                null, List.of(), 0, 0, T1, T2, null, null, 0, 0, Set.of(), Set.of());
    }

    private static final class FakePromotionRepository implements PromotionRepository {

        private final Map<PromotionId, Promotion> byId = new HashMap<>();

        @Override
        public Promotion save(Promotion promotion) {
            byId.put(promotion.getId(), promotion);
            return promotion;
        }

        @Override
        public Optional<Promotion> findById(PromotionId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Promotion> findByIdForUpdate(PromotionId id) {
            return findById(id);
        }

        @Override
        public List<Promotion> findAll() {
            return List.copyOf(byId.values());
        }

        @Override
        public boolean existsById(PromotionId id) {
            return byId.containsKey(id);
        }
    }
}