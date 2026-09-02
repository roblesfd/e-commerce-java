package org.fernandodev.ecommerce.checkout.adapter.out;

import java.time.Instant;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.checkout.application.port.out.PriceProvider;
import org.fernandodev.ecommerce.pricing.application.port.in.PricingUseCases;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador que implementa {@link PriceProvider} consultando el contexto
 * Pricing.
 */
@Component
@RequiredArgsConstructor
public class CheckoutPricingPriceProvider implements PriceProvider {

    private final PricingUseCases pricingUseCases;

    @Override
    public Money currentPrice(ProductId productId, ProductVariantId variantId) {
        return pricingUseCases.currentPrice(productId, variantId, Instant.now());
    }
}