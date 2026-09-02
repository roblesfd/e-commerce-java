package org.fernandodev.ecommerce.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.domain.value_object.InventoryId;
import org.fernandodev.ecommerce.inventory.domain.value_object.ReservationStatus;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockMovementType;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservation;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservationId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class InventoryTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T1 = Instant.parse("2026-01-02T00:00:00Z");

    @Test
    void defineCreaInventarioConStockDisponible() {
        Inventory inventory = inventario(10);

        assertThat(inventory.availableQuantity()).isEqualTo(10);
        assertThat(inventory.getReservations()).isEmpty();
        assertThat(inventory.getMovements()).isEmpty();
    }

    @Test
    void availableEsOnHandMenosReservado() {
        Inventory inventory = inventario(10);
        inventory.reserve(4, T1, T0);

        assertThat(inventory.availableQuantity()).isEqualTo(6);
    }

    @Test
    void increaseIncrementaStockYRegistraMovimiento() {
        Inventory inventory = inventario(10);

        inventory.increase(5, "reposición", T0);

        assertThat(inventory.availableQuantity()).isEqualTo(15);
        assertThat(inventory.getMovements()).hasSize(1);
        assertThat(inventory.getMovements().get(0).type()).isEqualTo(StockMovementType.INCREASE);
        assertThat(inventory.getMovements().get(0).quantity()).isEqualTo(5);
    }

    @Test
    void decreaseReduceStockYRegistraMovimiento() {
        Inventory inventory = inventario(10);

        inventory.decrease(3, "venta", T0);

        assertThat(inventory.availableQuantity()).isEqualTo(7);
        assertThat(inventory.getMovements().get(0).type()).isEqualTo(StockMovementType.DECREASE);
        assertThat(inventory.getMovements().get(0).quantity()).isEqualTo(-3);
    }

    @Test
    void decreaseMayorQueElStockLanzaExcepcion() {
        Inventory inventory = inventario(10);

        assertThatThrownBy(() -> inventory.decrease(11, "venta", T0))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void decreaseNoDejaDisponibleNegativo() {
        Inventory inventory = inventario(10);
        inventory.reserve(8, T1, T0);

        assertThatThrownBy(() -> inventory.decrease(5, "venta", T0))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void adjustEstableceElObjetivoYRegistraElDelta() {
        Inventory inventory = inventario(10);

        inventory.adjust(25, "conteo físico", T0);

        assertThat(inventory.availableQuantity()).isEqualTo(25);
        assertThat(inventory.getMovements().get(0).type()).isEqualTo(StockMovementType.ADJUST);
        assertThat(inventory.getMovements().get(0).quantity()).isEqualTo(15);
    }

    @Test
    void adjustPorDebajoDeLoReservadoLanzaExcepcion() {
        Inventory inventory = inventario(10);
        inventory.reserve(8, T1, T0);

        assertThatThrownBy(() -> inventory.adjust(5, "conteo", T0))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void setMinimumStockCambiaElUmbral() {
        Inventory inventory = inventario(10);

        inventory.setMinimumStock(5);

        assertThat(inventory.getMinimumStock()).isEqualTo(5);
    }

    @Test
    void isLowStockCuandoDisponibleMenorOIgualAlMinimo() {
        Inventory inventory = inventario(10);
        inventory.setMinimumStock(7);

        inventory.reserve(4, T1, T0);

        assertThat(inventory.availableQuantity()).isEqualTo(6);
        assertThat(inventory.isLowStock()).isTrue();
    }

    @Test
    void reserveCreaReservaActivaYSubeReservado() {
        Inventory inventory = inventario(10);

        StockReservation reservation = inventory.reserve(4, T1, T0);

        assertThat(reservation.status()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(inventory.getReservations()).hasSize(1);
        assertThat(inventory.availableQuantity()).isEqualTo(6);
        assertThat(inventory.getMovements().get(0).type()).isEqualTo(StockMovementType.RESERVE);
    }

    @Test
    void reserveMayorQueDisponiblePrevieneOverselling() {
        Inventory inventory = inventario(2);

        assertThatThrownBy(() -> inventory.reserve(3, T1, T0))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void reserveConExpiryEnElPasadoLanzaExcepcion() {
        Inventory inventory = inventario(10);

        assertThatThrownBy(() -> inventory.reserve(1, T0, T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void confirmReservationDescuentaReservadoYOnHand() {
        Inventory inventory = inventario(10);
        StockReservation reservation = inventory.reserve(4, T1, T0);

        inventory.confirmReservation(reservation.id(), T1);

        assertThat(inventory.getReservations().get(0).status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(inventory.availableQuantity()).isEqualTo(6);
        assertThat(inventory.getOnHand()).isEqualTo(6);
    }

    @Test
    void releaseReservationDevuelveElStock() {
        Inventory inventory = inventario(10);
        StockReservation reservation = inventory.reserve(4, T1, T0);

        inventory.releaseReservation(reservation.id(), T0);

        assertThat(inventory.getReservations().get(0).status()).isEqualTo(ReservationStatus.RELEASED);
        assertThat(inventory.availableQuantity()).isEqualTo(10);
    }

    @Test
    void expireReservationDevuelveElStock() {
        Inventory inventory = inventario(10);
        StockReservation reservation = inventory.reserve(4, T1, T0);

        inventory.expireReservation(reservation.id(), T1);

        assertThat(inventory.getReservations().get(0).status()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(inventory.availableQuantity()).isEqualTo(10);
    }

    @Test
    void confirmarReservaNoActivaLanzaExcepcion() {
        Inventory inventory = inventario(10);
        StockReservation reservation = inventory.reserve(4, T1, T0);
        inventory.releaseReservation(reservation.id(), T0);

        assertThatThrownBy(() -> inventory.confirmReservation(reservation.id(), T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void operarSobreReservaInexistenteLanzaExcepcion() {
        Inventory inventory = inventario(10);

        assertThatThrownBy(() -> inventory.releaseReservation(StockReservationId.generate(), T0))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void movementsRegistranAuditoriaCompletaDeReservas() {
        Inventory inventory = inventario(10);
        StockReservation reservation = inventory.reserve(4, T1, T0);
        inventory.confirmReservation(reservation.id(), T1);

        assertThat(inventory.getMovements())
                .extracting(m -> m.type())
                .containsExactly(StockMovementType.RESERVE, StockMovementType.CONFIRM);
    }

    private static Inventory inventario(int onHand) {
        return Inventory.define(InventoryId.generate(), ProductId.generate(), null, onHand, 0);
    }
}