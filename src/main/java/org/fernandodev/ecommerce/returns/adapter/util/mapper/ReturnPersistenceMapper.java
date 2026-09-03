package org.fernandodev.ecommerce.returns.adapter.util.mapper;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa.ReturnItemEmbeddable;
import org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa.ReturnStatusHistoryEmbeddable;
import org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa.entity.ReturnJpaEntity;
import org.fernandodev.ecommerce.returns.domain.Return;
import org.fernandodev.ecommerce.returns.domain.value_object.InspectionResult;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnItem;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnStatusEntry;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Mapeo entre el agregado {@link Return} y su entidad JPA.
 */
public final class ReturnPersistenceMapper {

    private ReturnPersistenceMapper() {
    }

    public static ReturnJpaEntity toEntity(Return aReturn) {
        ReturnJpaEntity entity = new ReturnJpaEntity();
        entity.setId(aReturn.getId().value());
        entity.setOrderId(aReturn.getOrderId().value());
        entity.setCustomerId(aReturn.getCustomerId());
        entity.setReason(aReturn.getReason());
        entity.setStatus(aReturn.getStatus());
        entity.setTrackingNumber(aReturn.getTrackingNumber());
        entity.setRefundAmount(aReturn.getRefundAmount() == null ? null : aReturn.getRefundAmount().amount());
        entity.setRefundCurrency(aReturn.getRefundAmount() == null ? null : aReturn.getRefundAmount().currencyCode());
        entity.setRefundStatus(aReturn.getRefundStatus());
        entity.setRefundRequested(aReturn.isRefundRequested());
        entity.setInspectionCondition(
                aReturn.getInspection() == null ? null : aReturn.getInspection().condition());
        entity.setInspectionNotes(aReturn.getInspection() == null ? null : aReturn.getInspection().notes());
        entity.setInspectionPassed(
                aReturn.getInspection() == null ? null : aReturn.getInspection().passed());
        entity.setInspectionAt(aReturn.getInspection() == null ? null : aReturn.getInspection().at());
        entity.setCreatedAt(aReturn.getCreatedAt());
        entity.setItems(aReturn.getItems().stream().map(ReturnPersistenceMapper::toItemEmbeddable).toList());
        entity.setStatusHistory(aReturn.getStatusHistory().stream()
                .map(ReturnPersistenceMapper::toHistoryEmbeddable).toList());
        return entity;
    }

    public static Return toDomain(ReturnJpaEntity entity) {
        List<ReturnItem> items = entity.getItems().stream()
                .map(ReturnPersistenceMapper::toReturnItem)
                .toList();
        List<ReturnStatusEntry> history = entity.getStatusHistory().stream()
                .map(ReturnPersistenceMapper::toStatusEntry)
                .toList();
        InspectionResult inspection = entity.getInspectionAt() == null
                ? null
                : new InspectionResult(entity.getInspectionCondition(), entity.getInspectionNotes(),
                        Boolean.TRUE.equals(entity.getInspectionPassed()), entity.getInspectionAt());
        Money refundAmount = entity.getRefundAmount() == null || entity.getRefundCurrency() == null
                ? null
                : Money.of(entity.getRefundAmount(), entity.getRefundCurrency());
        return Return.crear(
                ReturnId.of(entity.getId()),
                OrderId.of(entity.getOrderId()),
                entity.getCustomerId(),
                entity.getReason(),
                items,
                entity.getStatus(),
                entity.getTrackingNumber(),
                refundAmount,
                entity.getRefundStatus(),
                entity.isRefundRequested(),
                inspection,
                history,
                entity.getCreatedAt(),
                entity.getUpdatedAt() == null ? entity.getCreatedAt() : entity.getUpdatedAt());
    }

    private static ReturnItemEmbeddable toItemEmbeddable(ReturnItem item) {
        ReturnItemEmbeddable embeddable = new ReturnItemEmbeddable();
        embeddable.setOrderItemId(item.orderItemId());
        embeddable.setProductId(item.productId().value());
        embeddable.setQuantity(item.quantity());
        embeddable.setRefundAmount(item.refundAmount().amount());
        embeddable.setCurrency(item.refundAmount().currencyCode());
        return embeddable;
    }

    private static ReturnItem toReturnItem(ReturnItemEmbeddable embeddable) {
        return new ReturnItem(embeddable.getOrderItemId(), ProductId.of(embeddable.getProductId()),
                embeddable.getQuantity(), Money.of(embeddable.getRefundAmount(), embeddable.getCurrency()));
    }

    private static ReturnStatusHistoryEmbeddable toHistoryEmbeddable(ReturnStatusEntry entry) {
        ReturnStatusHistoryEmbeddable embeddable = new ReturnStatusHistoryEmbeddable();
        embeddable.setStatus(entry.status());
        embeddable.setAt(entry.at());
        return embeddable;
    }

    private static ReturnStatusEntry toStatusEntry(ReturnStatusHistoryEmbeddable embeddable) {
        return new ReturnStatusEntry(embeddable.getStatus(), embeddable.getAt());
    }
}