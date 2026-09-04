package org.fernandodev.ecommerce.inventory.adapter.util.mapper;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.adapter.out.persistence.jpa.StockMovementEmbeddable;
import org.fernandodev.ecommerce.inventory.adapter.out.persistence.jpa.entity.InventoryJpaEntity;
import org.fernandodev.ecommerce.inventory.adapter.out.persistence.jpa.entity.StockReservationJpaEntity;
import org.fernandodev.ecommerce.inventory.domain.Inventory;
import org.fernandodev.ecommerce.inventory.domain.value_object.InventoryId;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockMovement;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservation;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservationId;

/**
 * Mapper manual entre el agregado de dominio {@link Inventory} y la entidad JPA
 * {@link InventoryJpaEntity}. Sin lógica de negocio.
 */
public final class InventoryPersistenceMapper {

    private InventoryPersistenceMapper() {
    }

    /**
     * @param inventory agregado de dominio.
     * @return entidad JPA equivalente.
     */
    public static InventoryJpaEntity toEntity(Inventory inventory) {
        InventoryJpaEntity entity = new InventoryJpaEntity();
        entity.setId(inventory.getId().value());
        entity.setProductId(inventory.getProductId().value());
        entity.setVariantId(inventory.getVariantId().map(ProductVariantId::value).orElse(null));
        entity.setOnHand(inventory.getOnHand());
        entity.setReserved(inventory.getReserved());
        entity.setMinimumStock(inventory.getMinimumStock());
        inventory.getMovements().forEach(movement -> entity.getMovements().add(toMovementEmbeddable(movement)));
        inventory.getReservations().forEach(reservation -> entity.getReservations().add(toReservationEntity(reservation, entity)));
        return entity;
    }

    /**
     * @param entity entidad JPA.
     * @return agregado de dominio equivalente.
     */
    public static Inventory toDomain(InventoryJpaEntity entity) {
        return Inventory.crear(
                InventoryId.of(entity.getId()),
                ProductId.of(entity.getProductId()),
                entity.getVariantId() == null ? null : ProductVariantId.of(entity.getVariantId()),
                entity.getOnHand(),
                entity.getReserved(),
                entity.getMinimumStock(),
                entity.getMovements().stream().map(InventoryPersistenceMapper::toDomainMovement).toList(),
                entity.getReservations().stream().map(InventoryPersistenceMapper::toDomainReservation).toList());
    }

    private static StockMovementEmbeddable toMovementEmbeddable(StockMovement movement) {
        StockMovementEmbeddable embeddable = new StockMovementEmbeddable();
        embeddable.setType(movement.type());
        embeddable.setQuantity(movement.quantity());
        embeddable.setReason(movement.reason());
        embeddable.setAt(movement.at());
        return embeddable;
    }

    private static StockReservationJpaEntity toReservationEntity(StockReservation reservation, InventoryJpaEntity inventory) {
        StockReservationJpaEntity entity = new StockReservationJpaEntity();
        entity.setId(reservation.id().value());
        entity.setInventory(inventory);
        entity.setQuantity(reservation.quantity());
        entity.setStatus(reservation.status());
        entity.setCreatedAt(reservation.createdAt());
        entity.setExpiresAt(reservation.expiresAt());
        return entity;
    }

    private static StockMovement toDomainMovement(StockMovementEmbeddable embeddable) {
        return new StockMovement(embeddable.getType(), embeddable.getQuantity(), embeddable.getReason(), embeddable.getAt());
    }

    private static StockReservation toDomainReservation(StockReservationJpaEntity entity) {
        return new StockReservation(
                StockReservationId.of(entity.getId()),
                entity.getQuantity(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExpiresAt());
    }
}