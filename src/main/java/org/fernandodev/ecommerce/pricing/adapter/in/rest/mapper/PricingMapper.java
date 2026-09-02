package org.fernandodev.ecommerce.pricing.adapter.in.rest.mapper;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.adapter.in.rest.dto.AddOverrideRequest;
import org.fernandodev.ecommerce.pricing.adapter.in.rest.dto.CurrentPriceResponse;
import org.fernandodev.ecommerce.pricing.adapter.in.rest.dto.DefinePriceRequest;
import org.fernandodev.ecommerce.pricing.adapter.in.rest.dto.PriceResponse;
import org.fernandodev.ecommerce.pricing.adapter.in.rest.dto.PriceSnapshotResponse;
import org.fernandodev.ecommerce.pricing.adapter.in.rest.dto.UpdatePriceRequest;
import org.fernandodev.ecommerce.pricing.application.dto.AddPriceOverrideCommand;
import org.fernandodev.ecommerce.pricing.application.dto.DefinePriceCommand;
import org.fernandodev.ecommerce.pricing.application.dto.PriceSnapshot;
import org.fernandodev.ecommerce.pricing.application.dto.UpdatePriceCommand;
import org.fernandodev.ecommerce.pricing.domain.Price;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceOverride;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

/** 
 * Mapper manual entre DTOs REST y objetos de aplicación/dominio del contexto
 * Pricing. Sin lógica de negocio.
 */
@Component
public class PricingMapper {

    public DefinePriceCommand toCommand(UUID productId, DefinePriceRequest request) {
        return new DefinePriceCommand(
                ProductId.of(productId),
                request.variantId() == null ? null : ProductVariantId.of(request.variantId()),
                request.amount(),
                request.currency());
    }

    public UpdatePriceCommand toCommand(UpdatePriceRequest request) {
        return new UpdatePriceCommand(request.amount(), request.currency());
    }

    public AddPriceOverrideCommand toCommand(AddOverrideRequest request) {
        return new AddPriceOverrideCommand(
                request.amount(),
                request.currency(),
                request.validFrom(),
                request.validTo(),
                request.type());
    }

    public PriceResponse toResponse(Price price) {
        return new PriceResponse(
                price.getId().value(),
                price.getProductId().value(),
                price.getVariantId().map(ProductVariantId::value).orElse(null),
                toMoneyResponse(price.getBasePrice()),
                price.getHistory().stream()
                        .map(change -> new PriceResponse.PriceChangeResponse(
                                change.amount().amount(),
                                change.amount().currencyCode(),
                                change.effectiveAt()))
                        .toList(),
                price.getOverrides().stream().map(this::toOverrideResponse).toList());
    }

    public CurrentPriceResponse toCurrentPriceResponse(Money price, Instant at) {
        return new CurrentPriceResponse(price.amount(), price.currencyCode(), at);
    }

    public PriceSnapshotResponse toSnapshotResponse(PriceSnapshot snapshot) {
        return new PriceSnapshotResponse(
                snapshot.productId().value(),
                snapshot.variantId() == null ? null : snapshot.variantId().value(),
                toMoney(snapshot.currentPrice()),
                toMoney(snapshot.basePrice()),
                toMoney(snapshot.discount()),
                snapshot.at());
    }

    private PriceResponse.PriceOverrideResponse toOverrideResponse(PriceOverride override) {
        return new PriceResponse.PriceOverrideResponse(
                override.id().value(),
                override.amount().amount(),
                override.amount().currencyCode(),
                override.validFrom(),
                override.validTo(),
                override.type().name());
    }

    private static PriceResponse.MoneyResponse toMoneyResponse(Money money) {
        return new PriceResponse.MoneyResponse(money.amount(), money.currencyCode());
    }

    private static PriceSnapshotResponse.Money toMoney(Money money) {
        return new PriceSnapshotResponse.Money(money.amount(), money.currencyCode());
    }
}
