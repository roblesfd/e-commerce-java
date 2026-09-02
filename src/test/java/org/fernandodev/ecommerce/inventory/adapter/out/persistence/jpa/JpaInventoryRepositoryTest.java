package org.fernandodev.ecommerce.inventory.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.application.port.out.InventoryRepository;
import org.fernandodev.ecommerce.inventory.domain.Inventory;
import org.fernandodev.ecommerce.inventory.domain.value_object.InventoryId;
import org.fernandodev.ecommerce.inventory.domain.value_object.ReservationStatus;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockMovementType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaInventoryRepositoryTest extends AbstractIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-09-01T00:00:00Z");
    private static final Instant EXPIRES = Instant.parse("2099-12-31T00:00:00Z");

    @Autowired
    private InventoryRepository repository;

    @Test
    void guardaYRecuperaInventarioConMovimientosYReservas() {
        ProductId productId = ProductId.generate();
        Inventory inventory = Inventory.define(InventoryId.generate(), productId, null, 10, 2);
        inventory.increase(5, "reposición", NOW);
        inventory.reserve(3, EXPIRES, NOW);

        repository.save(inventory);
        Inventory loaded = repository.findById(inventory.getId()).orElseThrow();

        assertThat(loaded.getProductId()).isEqualTo(productId);
        assertThat(loaded.availableQuantity()).isEqualTo(12);
        assertThat(loaded.getMinimumStock()).isEqualTo(2);
        assertThat(loaded.getMovements())
                .extracting(m -> m.type())
                .contains(StockMovementType.INCREASE, StockMovementType.RESERVE);
        assertThat(loaded.getReservations()).hasSize(1);
        assertThat(loaded.getReservations().get(0).status()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(loaded.getReservations().get(0).expiresAt()).isEqualTo(EXPIRES);
    }

    @Test
    void existsByProductAndVariantTrasGuardar() {
        ProductId productId = ProductId.generate();
        repository.save(Inventory.define(InventoryId.generate(), productId, null, 10, 0));

        assertThat(repository.existsByProductAndVariant(productId, null)).isTrue();
        assertThat(repository.existsByProductAndVariant(ProductId.generate(), null)).isFalse();
    }

    @Test
    void findByProductAndVariantDistingueVarianteYProducto() {
        ProductId productId = ProductId.generate();
        ProductVariantId variantId = ProductVariantId.generate();
        repository.save(Inventory.define(InventoryId.generate(), productId, null, 10, 0));
        repository.save(Inventory.define(InventoryId.generate(), productId, variantId, 3, 0));

        assertThat(repository.findByProductAndVariant(productId, null).orElseThrow().availableQuantity())
                .isEqualTo(10);
        assertThat(repository.findByProductAndVariant(productId, variantId).orElseThrow().availableQuantity())
                .isEqualTo(3);
    }

    @Test
    void findByProductAndVariantForUpdateDevuelveElInventario() {
        ProductId productId = ProductId.generate();
        Inventory inventory = repository.save(Inventory.define(InventoryId.generate(), productId, null, 10, 0));

        assertThat(repository.findByProductAndVariantForUpdate(productId, null).orElseThrow().getId())
                .isEqualTo(inventory.getId());
    }

    @Test
    void savePersisteElCambioDeEstadoDeLaReserva() {
        ProductId productId = ProductId.generate();
        Inventory inventory = repository.save(Inventory.define(InventoryId.generate(), productId, null, 10, 0));
        var reservation = inventory.reserve(3, EXPIRES, NOW);
        repository.save(inventory);

        Inventory loaded = repository.findById(inventory.getId()).orElseThrow();
        loaded.confirmReservation(reservation.id(), NOW);
        repository.save(loaded);

        Inventory afterConfirm = repository.findById(inventory.getId()).orElseThrow();
        assertThat(afterConfirm.getOnHand()).isEqualTo(7);
        assertThat(afterConfirm.getReservations().get(0).status()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}