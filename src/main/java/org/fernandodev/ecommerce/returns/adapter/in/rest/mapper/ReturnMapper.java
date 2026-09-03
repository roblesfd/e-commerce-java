package org.fernandodev.ecommerce.returns.adapter.in.rest.mapper;

import java.util.List;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.CreateReturnRequest;
import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.ReturnResponse;
import org.fernandodev.ecommerce.returns.application.dto.CreateReturnCommand;
import org.fernandodev.ecommerce.returns.domain.Return;
import org.springframework.stereotype.Component;

/**
 * Mapeo entre el agregado {@link Return} y sus DTOs REST.
 */
@Component
public class ReturnMapper {

    public CreateReturnCommand toCommand(CreateReturnRequest request) {
        return new CreateReturnCommand(request.reason(),
                request.items().stream()
                        .map(item -> new CreateReturnCommand.ReturnItemCommand(item.orderItemId(), item.quantity()))
                        .toList());
    }

    public ReturnResponse toResponse(Return aReturn) {
        ReturnResponse.InspectionResponse inspection = aReturn.getInspection() == null
                ? null
                : new ReturnResponse.InspectionResponse(
                        aReturn.getInspection().condition(),
                        aReturn.getInspection().notes(),
                        aReturn.getInspection().passed(),
                        aReturn.getInspection().at());
        List<ReturnResponse.ItemResponse> items = aReturn.getItems().stream()
                .map(item -> new ReturnResponse.ItemResponse(
                        item.orderItemId(), item.productId().value(), item.quantity(),
                        item.refundAmount().amount(), item.refundAmount().currencyCode()))
                .toList();
        List<ReturnResponse.StatusEntryResponse> history = aReturn.getStatusHistory().stream()
                .map(entry -> new ReturnResponse.StatusEntryResponse(entry.status(), entry.at()))
                .toList();
        return new ReturnResponse(
                aReturn.getId().value(),
                aReturn.getOrderId().value(),
                aReturn.getCustomerId(),
                aReturn.getReason(),
                aReturn.getStatus(),
                aReturn.getTrackingNumber(),
                aReturn.getRefundAmount() == null ? null : aReturn.getRefundAmount().amount(),
                aReturn.getRefundAmount() == null ? null : aReturn.getRefundAmount().currencyCode(),
                aReturn.getRefundStatus(),
                aReturn.isRefundRequested(),
                inspection,
                items,
                history,
                aReturn.getCreatedAt(),
                aReturn.getUpdatedAt());
    }
}