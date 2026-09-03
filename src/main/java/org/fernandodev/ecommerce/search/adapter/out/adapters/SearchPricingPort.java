package org.fernandodev.ecommerce.search.adapter.out.adapters;

import java.time.Instant;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.application.port.in.PricingUseCases;
import org.fernandodev.ecommerce.search.application.port.out.PricingPort;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador del puerto {@link PricingPort} hacia el contexto Pricing.
 */
@Component
@RequiredArgsConstructor
public class SearchPricingPort implements PricingPort {

    private final PricingUseCases pricing;

    @Override
    public Money currentPrice(ProductId productId, ProductVariantId variantId) {
        return pricing.currentPrice(productId, variantId, Instant.now());
    }
}