package org.fernandodev.ecommerce.pricing.application.service;

import java.time.Instant;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.application.dto.AddPriceOverrideCommand;
import org.fernandodev.ecommerce.pricing.application.dto.DefinePriceCommand;
import org.fernandodev.ecommerce.pricing.application.dto.PriceSnapshot;
import org.fernandodev.ecommerce.pricing.application.dto.UpdatePriceCommand;
import org.fernandodev.ecommerce.pricing.application.port.in.PricingUseCases;
import org.fernandodev.ecommerce.pricing.application.port.out.PriceRepository;
import org.fernandodev.ecommerce.pricing.domain.Price;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceOverrideId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación del contexto Pricing.
 * <p>
 * Orquesta los casos de uso y aplica reglas de aplicación (unicidad de precio
 * por producto/variante y resolución variante → producto) sobre el puerto de
 * persistencia. No depende de Spring.
 * </p>
 */
@RequiredArgsConstructor
public class PricingService implements PricingUseCases {

    private final PriceRepository prices;

    /**

     * {@inheritDoc}

     */

    @Override
    public Price defineBasePrice(DefinePriceCommand command) {
        if (prices.existsByProductAndVariant(command.productId(), command.variantId())) {
            throw new ConflictException(
                    "Ya existe un precio para el producto/variante: " + command.productId());
        }
        Price price = Price.define(
                PriceId.generate(),
                command.productId(),
                command.variantId(),
                Money.of(command.amount(), command.currency()));
        return prices.save(price);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public Price updateBasePrice(PriceId id, UpdatePriceCommand command) {
        Price price = getExisting(id);
        price.updateBasePrice(Money.of(command.amount(), command.currency()), Instant.now());
        return prices.save(price);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public Price getById(PriceId id) {
        return getExisting(id);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public Price addOverride(PriceId id, AddPriceOverrideCommand command) {
        Price price = getExisting(id);
        price.addOverride(
                Money.of(command.amount(), command.currency()),
                command.validFrom(),
                command.validTo(),
                command.type());
        return prices.save(price);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public Price removeOverride(PriceId id, PriceOverrideId overrideId) {
        Price price = getExisting(id);
        price.removeOverride(overrideId);
        return prices.save(price);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public Money currentPrice(ProductId productId, ProductVariantId variantId, Instant at) {
        return resolve(productId, variantId).currentPrice(at);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public Money discount(ProductId productId, ProductVariantId variantId, Instant at) {
        return resolve(productId, variantId).discountAt(at);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public PriceSnapshot priceSnapshot(ProductId productId, ProductVariantId variantId, Instant at) {
        Price price = resolve(productId, variantId);
        Money current = price.currentPrice(at);
        return new PriceSnapshot(
                productId,
                variantId,
                price.getBasePrice(),
                current,
                price.getBasePrice().subtract(current),
                at);
    }

    private Price getExisting(PriceId id) {
        return prices.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Precio no encontrado: " + id));
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public Price getByProduct(ProductId productId, ProductVariantId variantId) {
        return resolve(productId, variantId);
    }

    private Price resolve(ProductId productId, ProductVariantId variantId) {
        Optional<Price> price = variantId == null
                ? prices.findByProductAndVariant(productId, null)
                : prices.findByProductAndVariant(productId, variantId)
                        .or(() -> prices.findByProductAndVariant(productId, null));
        return price.orElseThrow(
                () -> new ResourceNotFoundException("Precio no definido para el producto: " + productId));
    }
}