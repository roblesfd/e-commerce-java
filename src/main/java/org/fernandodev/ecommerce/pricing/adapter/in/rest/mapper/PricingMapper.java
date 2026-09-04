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
 * {@code Pricing}.
 *
 * <p>Su responsabilidad se limita a transformar datos entre las capas REST
 * y aplicación/dominio. No contiene lógica de negocio ni reglas de validación
 * propias del dominio.</p>
 */
@Component
public class PricingMapper {

    /**
     * Convierte una petición REST en el comando de aplicación utilizado para
     * definir el precio de un producto o de una variante.
     *
     * @param productId identificador del producto al que pertenece el precio
     * @param request datos necesarios para definir el precio
     * @return comando de aplicación para definir el precio
     */
    public DefinePriceCommand toCommand(UUID productId, DefinePriceRequest request) {
        return new DefinePriceCommand(
                ProductId.of(productId),
                request.variantId() == null ? null : ProductVariantId.of(request.variantId()),
                request.amount(),
                request.currency());
    }

    /**
     * Convierte una petición REST en el comando de aplicación utilizado para
     * actualizar el precio base existente.
     *
     * @param request datos del nuevo precio
     * @return comando de aplicación para actualizar el precio
     */
    public UpdatePriceCommand toCommand(UpdatePriceRequest request) {
        return new UpdatePriceCommand(request.amount(), request.currency());
    }

    /**
     * Convierte una petición REST en el comando de aplicación utilizado para
     * agregar una modificación de precio con vigencia determinada.
     *
     * @param request datos del override de precio
     * @return comando de aplicación para agregar el override
     */
    public AddPriceOverrideCommand toCommand(AddOverrideRequest request) {
        return new AddPriceOverrideCommand(
                request.amount(),
                request.currency(),
                request.validFrom(),
                request.validTo(),
                request.type());
    }

    /**
     * Convierte una entidad de dominio {@link Price} en su representación
     * correspondiente para la API REST.
     *
     * <p>Incluye el precio base, el historial de cambios y los overrides
     * asociados al precio.</p>
     *
     * @param price precio del dominio que será expuesto por la API
     * @return respuesta REST con la información completa del precio
     */
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
                price.getOverrides().stream()
                        .map(this::toOverrideResponse)
                        .toList());
    }

    /**
     * Convierte un importe monetario vigente en la respuesta REST utilizada
     * para consultar el precio actual.
     *
     * @param price precio vigente
     * @param at instante en el que se determinó la vigencia del precio
     * @return respuesta REST con el precio vigente y el instante de consulta
     */
    public CurrentPriceResponse toCurrentPriceResponse(Money price, Instant at) {
        return new CurrentPriceResponse(
                price.amount(),
                price.currencyCode(),
                at);
    }

    /**
     * Convierte una instantánea de precio del dominio en su representación
     * correspondiente para la API REST.
     *
     * @param snapshot instantánea del precio
     * @return respuesta REST con el estado del precio en el instante indicado
     */
    public PriceSnapshotResponse toSnapshotResponse(PriceSnapshot snapshot) {
        return new PriceSnapshotResponse(
                snapshot.productId().value(),
                snapshot.variantId() == null ? null : snapshot.variantId().value(),
                toMoney(snapshot.currentPrice()),
                toMoney(snapshot.basePrice()),
                toMoney(snapshot.discount()),
                snapshot.at());
    }

    /**
     * Convierte un override de precio del dominio en su representación REST.
     *
     * @param override override de precio
     * @return representación REST del override
     */
    private PriceResponse.PriceOverrideResponse toOverrideResponse(PriceOverride override) {
        return new PriceResponse.PriceOverrideResponse(
                override.id().value(),
                override.amount().amount(),
                override.amount().currencyCode(),
                override.validFrom(),
                override.validTo(),
                override.type().name());
    }

    /**
     * Convierte un {@link Money} del dominio en su representación REST.
     *
     * @param money valor monetario del dominio
     * @return representación REST del valor monetario
     */
    private static PriceResponse.MoneyResponse toMoneyResponse(Money money) {
        return new PriceResponse.MoneyResponse(
                money.amount(),
                money.currencyCode());
    }

    /**
     * Convierte un {@link Money} del dominio en el objeto monetario utilizado
     * dentro de una instantánea de precio.
     *
     * @param money valor monetario del dominio
     * @return representación monetaria de la instantánea
     */
    private static PriceSnapshotResponse.Money toMoney(Money money) {
        return new PriceSnapshotResponse.Money(
                money.amount(),
                money.currencyCode());
    }
}
