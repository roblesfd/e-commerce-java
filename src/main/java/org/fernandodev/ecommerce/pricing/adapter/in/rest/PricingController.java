package org.fernandodev.ecommerce.pricing.adapter.in.rest;

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
import org.fernandodev.ecommerce.pricing.adapter.in.rest.mapper.PricingMapper;
import org.fernandodev.ecommerce.pricing.application.port.in.PricingUseCases;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceOverrideId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST del contexto Pricing.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PricingController {

    private final PricingUseCases pricingUseCases;
    private final PricingMapper mapper;

    @PostMapping("/products/{productId}/prices")
    @ResponseStatus(HttpStatus.CREATED)
    public PriceResponse defineBasePrice(@PathVariable UUID productId,
                                         @Valid @RequestBody DefinePriceRequest request) {
        return mapper.toResponse(
                pricingUseCases.defineBasePrice(
                        mapper.toCommand(productId, request)
                )
        );
    }

    @GetMapping("/products/{productId}/prices/current")
    /**
     * Current price.
     * @param productId productId.
     * @param variantId variantId.
     */
    public CurrentPriceResponse currentPrice(@PathVariable UUID productId,
                                             @RequestParam(required = false) UUID variantId) {
        Instant at = Instant.now();
        return mapper.toCurrentPriceResponse(
                pricingUseCases.currentPrice(
                        ProductId.of(productId),
                        variant(variantId),
                        at
                ),
                at
        );
    }

    @GetMapping("/products/{productId}/prices/snapshot")
    /**
     * Price snapshot.
     * @param productId productId.
     * @param variantId variantId.
     */
    public PriceSnapshotResponse priceSnapshot(@PathVariable UUID productId,
                                               @RequestParam(required = false) UUID variantId) {
        return mapper.toSnapshotResponse(
                pricingUseCases.priceSnapshot(
                        ProductId.of(productId),
                        variant(variantId),
                        Instant.now()
                )
        );
    }

    @GetMapping("/prices/{priceId}")
    /**
     * Get by id.
     * @param priceId priceId.
     */
    public PriceResponse getById(@PathVariable UUID priceId) {
        return mapper.toResponse(
                pricingUseCases.getById(PriceId.of(priceId))
        );
    }

    @PutMapping("/prices/{priceId}")
    /**
     * Update base price.
     * @param priceId priceId.
     * @param request request.
     */
    public PriceResponse updateBasePrice(@PathVariable UUID priceId,
                                         @Valid @RequestBody UpdatePriceRequest request) {
        return mapper.toResponse(
                pricingUseCases.updateBasePrice(
                        PriceId.of(priceId),
                        mapper.toCommand(request)
                )
        );
    }

    @PostMapping("/prices/{priceId}/overrides")
    @ResponseStatus(HttpStatus.CREATED)
    /**
     * Add override.
     * @param priceId priceId.
     * @param request request.
     */
    public PriceResponse addOverride(@PathVariable UUID priceId,
                                     @Valid @RequestBody AddOverrideRequest request) {
        return mapper.toResponse(
                pricingUseCases.addOverride(
                        PriceId.of(priceId),
                        mapper.toCommand(request))
        );
    }

    @DeleteMapping("/prices/{priceId}/overrides/{overrideId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Remove override.
     * @param priceId priceId.
     * @param overrideId overrideId.
     */
    public void removeOverride(@PathVariable UUID priceId, @PathVariable UUID overrideId) {
        pricingUseCases.removeOverride(
                PriceId.of(priceId),
                PriceOverrideId.of(overrideId)
        );
    }

    private static ProductVariantId variant(UUID variantId) {
        return variantId == null ? null : ProductVariantId.of(variantId);
    }
}
