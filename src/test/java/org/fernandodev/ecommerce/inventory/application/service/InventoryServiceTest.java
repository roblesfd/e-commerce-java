package org.fernandodev.ecommerce.inventory.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.application.dto.AdjustStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.DecreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.IncreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.ReserveStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.SetMinimumStockCommand;
import org.fernandodev.ecommerce.inventory.application.port.out.InventoryRepository;
import org.fernandodev.ecommerce.inventory.domain.Inventory;
import org.fernandodev.ecommerce.inventory.domain.value_object.InventoryId;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockMovementType;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservation;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservationId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class InventoryServiceTest {

    private static final Instant T1 = Instant.parse("2099-12-31T00:00:00Z");

    private final FakeInventoryRepository repository = new FakeInventoryRepository();
    private final InventoryService service = new InventoryService(repository);

    @Test
    void increaseCreaInventarioSiNoExiste() {
        ProductId productId = ProductId.generate();

        Inventory inventory = service.increaseStock(productId, null, new IncreaseStockCommand(10, "reposición"));

        assertThat(inventory.availableQuantity()).isEqualTo(10);
        assertThat(repository.findByProductAndVariant(productId, null)).contains(inventory);
    }

    @Test
    void increaseSobreExistenteAcumula() {
        ProductId productId = ProductId.generate();
        service.increaseStock(productId, null, new IncreaseStockCommand(10, "reposición"));

        Inventory inventory = service.increaseStock(productId, null, new IncreaseStockCommand(5, "reposición"));

        assertThat(inventory.availableQuantity()).isEqualTo(15);
    }

    @Test
    void decreaseReduceStock() {
        ProductId productId = ProductId.generate();
        service.increaseStock(productId, null, new IncreaseStockCommand(10, "reposición"));

        Inventory inventory = service.decreaseStock(productId, null, new DecreaseStockCommand(4, "venta"));

        assertThat(inventory.availableQuantity()).isEqualTo(6);
    }

    @Test
    void decreaseSinInventarioLanzaNotFound() {
        assertThatThrownBy(() -> service.decreaseStock(ProductId.generate(), null,
                new DecreaseStockCommand(1, "venta")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void adjustCambiaStockAlObjetivo() {
        ProductId productId = ProductId.generate();
        service.increaseStock(productId, null, new IncreaseStockCommand(10, "reposición"));

        Inventory inventory = service.adjustStock(productId, null, new AdjustStockCommand(25, "conteo"));

        assertThat(inventory.availableQuantity()).isEqualTo(25);
    }

    @Test
    void setMinimumStockCambiaElUmbral() {
        ProductId productId = ProductId.generate();
        service.increaseStock(productId, null, new IncreaseStockCommand(10, "reposición"));

        Inventory inventory = service.setMinimumStock(productId, null, new SetMinimumStockCommand(5));

        assertThat(inventory.getMinimumStock()).isEqualTo(5);
    }

    @Test
    void availableStockDevuelve0SinInventario() {
        assertThat(service.availableStock(ProductId.generate(), null)).isZero();
    }

    @Test
    void getByProductInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getByProduct(ProductId.generate(), null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void reserveCreaReservaYReduceDisponible() {
        ProductId productId = ProductId.generate();
        service.increaseStock(productId, null, new IncreaseStockCommand(10, "reposición"));

        StockReservation reservation = service.reserveStock(productId, null,
                new ReserveStockCommand(4, T1));

        assertThat(reservation).isNotNull();
        assertThat(service.availableStock(productId, null)).isEqualTo(6);
    }

    @Test
    void reserveSinStockSuficienteLanzaViolacion() {
        ProductId productId = ProductId.generate();
        service.increaseStock(productId, null, new IncreaseStockCommand(1, "reposición"));

        assertThatThrownBy(() -> service.reserveStock(productId, null, new ReserveStockCommand(2, T1)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void reserveSinInventarioLanzaNotFound() {
        assertThatThrownBy(() -> service.reserveStock(ProductId.generate(), null, new ReserveStockCommand(1, T1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void confirmReservationConfirmaYDescuentaStock() {
        ProductId productId = ProductId.generate();
        service.increaseStock(productId, null, new IncreaseStockCommand(10, "reposición"));
        StockReservation reservation = service.reserveStock(productId, null, new ReserveStockCommand(4, T1));

        Inventory inventory = service.confirmReservation(productId, null, reservation.id());

        assertThat(inventory.getOnHand()).isEqualTo(6);
        assertThat(inventory.availableQuantity()).isEqualTo(6);
        assertThat(inventory.getReservations().get(0).isActive()).isFalse();
    }

    @Test
    void releaseReservationLiberaElStock() {
        ProductId productId = ProductId.generate();
        service.increaseStock(productId, null, new IncreaseStockCommand(10, "reposición"));
        StockReservation reservation = service.reserveStock(productId, null, new ReserveStockCommand(4, T1));

        Inventory inventory = service.releaseReservation(productId, null, reservation.id());

        assertThat(inventory.availableQuantity()).isEqualTo(10);
    }

    @Test
    void expireReservationExpiraYLiberaElStock() {
        ProductId productId = ProductId.generate();
        service.increaseStock(productId, null, new IncreaseStockCommand(10, "reposición"));
        StockReservation reservation = service.reserveStock(productId, null, new ReserveStockCommand(4, T1));

        Inventory inventory = service.expireReservation(productId, null, reservation.id());

        assertThat(inventory.availableQuantity()).isEqualTo(10);
    }

    @Test
    void confirmarReservaInexistenteLanzaNotFound() {
        ProductId productId = ProductId.generate();
        service.increaseStock(productId, null, new IncreaseStockCommand(10, "reposición"));

        assertThatThrownBy(() -> service.confirmReservation(productId, null, StockReservationId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void movementHistoryDevuelveLaAuditoria() {
        ProductId productId = ProductId.generate();
        Inventory inventory = service.increaseStock(productId, null, new IncreaseStockCommand(10, "reposición"));
        StockReservation reservation = service.reserveStock(productId, null, new ReserveStockCommand(4, T1));
        service.confirmReservation(productId, null, reservation.id());

        assertThat(inventory.getMovements()).isNotEmpty();
        assertThat(service.movementHistory(productId, null))
                .extracting(m -> m.type())
                .containsExactly(StockMovementType.INCREASE, StockMovementType.RESERVE, StockMovementType.CONFIRM);
    }

    private static final class FakeInventoryRepository implements InventoryRepository {

        private final Map<InventoryId, Inventory> byId = new HashMap<>();

        @Override
        public Inventory save(Inventory inventory) {
            byId.put(inventory.getId(), inventory);
            return inventory;
        }

        @Override
        public Optional<Inventory> findById(InventoryId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Inventory> findByProductAndVariant(ProductId productId, ProductVariantId variantId) {
            return byId.values().stream()
                    .filter(inventory -> inventory.getProductId().equals(productId))
                    .filter(inventory -> inventory.getVariantId().equals(Optional.ofNullable(variantId)))
                    .findFirst();
        }

        @Override
        public Optional<Inventory> findByProductAndVariantForUpdate(ProductId productId, ProductVariantId variantId) {
            return findByProductAndVariant(productId, variantId);
        }

        @Override
        public boolean existsByProductAndVariant(ProductId productId, ProductVariantId variantId) {
            return findByProductAndVariant(productId, variantId).isPresent();
        }
    }
}