package org.fernandodev.ecommerce.pricing.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.application.dto.AddPriceOverrideCommand;
import org.fernandodev.ecommerce.pricing.application.dto.DefinePriceCommand;
import org.fernandodev.ecommerce.pricing.application.dto.PriceSnapshot;
import org.fernandodev.ecommerce.pricing.application.dto.UpdatePriceCommand;
import org.fernandodev.ecommerce.pricing.application.port.out.PriceRepository;
import org.fernandodev.ecommerce.pricing.domain.Price;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceOverrideId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceType;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class PricingServiceTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2026-02-01T00:00:00Z");
    private static final Instant T3 = Instant.parse("2026-03-01T00:00:00Z");

    private final FakePriceRepository repository = new FakePriceRepository();
    private final PricingService service = new PricingService(repository);

    @Test
    void defineBasePriceGuardaYDevuelveElPrecio() {
        ProductId productId = ProductId.generate();

        Price price = service.defineBasePrice(new DefinePriceCommand(productId, null, amount(900), "USD"));

        assertThat(price.getId()).isNotNull();
        assertThat(price.getProductId()).isEqualTo(productId);
        assertThat(price.currentPrice(T1)).isEqualTo(usd(900));
        assertThat(repository.findById(price.getId())).contains(price);
    }

    @Test
    void defineConPrecioYaExistenteLanzaConflicto() {
        ProductId productId = ProductId.generate();
        service.defineBasePrice(new DefinePriceCommand(productId, null, amount(900), "USD"));

        assertThatThrownBy(() -> service.defineBasePrice(new DefinePriceCommand(productId, null, amount(800), "USD")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void defineNivelProductoYVariantePuedenCoexistir() {
        ProductId productId = ProductId.generate();
        ProductVariantId variantId = ProductVariantId.generate();
        service.defineBasePrice(new DefinePriceCommand(productId, null, amount(900), "USD"));

        Price variantPrice = service.defineBasePrice(new DefinePriceCommand(productId, variantId, amount(950), "USD"));

        assertThat(variantPrice.getVariantId()).contains(variantId);
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(PriceId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateBasePriceCambiaElVigente() {
        Price price = service.defineBasePrice(new DefinePriceCommand(ProductId.generate(), null, amount(900), "USD"));

        Price updated = service.updateBasePrice(price.getId(), new UpdatePriceCommand(amount(1000), "USD"));

        assertThat(updated.currentPrice(T3)).isEqualTo(usd(1000));
        assertThat(updated.getHistory()).hasSize(1);
    }

    @Test
    void currentPriceConVariantUsaPrecioDeVariante() {
        ProductId productId = ProductId.generate();
        ProductVariantId variantId = ProductVariantId.generate();
        service.defineBasePrice(new DefinePriceCommand(productId, null, amount(900), "USD"));
        service.defineBasePrice(new DefinePriceCommand(productId, variantId, amount(950), "USD"));

        assertThat(service.currentPrice(productId, variantId, T1)).isEqualTo(usd(950));
    }

    @Test
    void currentPriceSinPrecioDeVarianteUsaNivelProducto() {
        ProductId productId = ProductId.generate();
        ProductVariantId variantId = ProductVariantId.generate();
        service.defineBasePrice(new DefinePriceCommand(productId, null, amount(900), "USD"));

        assertThat(service.currentPrice(productId, variantId, T1)).isEqualTo(usd(900));
    }

    @Test
    void currentPriceSinPrecioDefinidoLanzaNotFound() {
        assertThatThrownBy(() -> service.currentPrice(ProductId.generate(), null, T1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addOverrideAplicaPromocion() {
        Price price = service.defineBasePrice(new DefinePriceCommand(ProductId.generate(), null, amount(900), "USD"));

        service.addOverride(price.getId(),
                new AddPriceOverrideCommand(amount(700), "USD", T1, T3, PriceType.PROMOTIONAL));

        assertThat(service.currentPrice(price.getProductId(), null, T2)).isEqualTo(usd(700));
    }

    @Test
    void removeOverrideRestauraElBase() {
        Price price = service.defineBasePrice(new DefinePriceCommand(ProductId.generate(), null, amount(900), "USD"));
        PriceOverrideId overrideId = service.addOverride(price.getId(),
                new AddPriceOverrideCommand(amount(700), "USD", T1, T3, PriceType.PROMOTIONAL))
                .getOverrides().get(0).id();

        service.removeOverride(price.getId(), overrideId);

        assertThat(service.currentPrice(price.getProductId(), null, T2)).isEqualTo(usd(900));
    }

    @Test
    void discountCalculaElDescuentoAplicado() {
        ProductId productId = ProductId.generate();
        Price price = service.defineBasePrice(new DefinePriceCommand(productId, null, amount(900), "USD"));
        service.addOverride(price.getId(),
                new AddPriceOverrideCommand(amount(700), "USD", T1, T3, PriceType.PROMOTIONAL));

        assertThat(service.discount(productId, null, T2)).isEqualTo(usd(200));
    }

    @Test
    void priceSnapshotDevuelveBaseVigenteYDescuento() {
        ProductId productId = ProductId.generate();
        Price price = service.defineBasePrice(new DefinePriceCommand(productId, null, amount(900), "USD"));
        service.addOverride(price.getId(),
                new AddPriceOverrideCommand(amount(700), "USD", T1, T3, PriceType.PROMOTIONAL));

        PriceSnapshot snapshot = service.priceSnapshot(productId, null, T2);

        assertThat(snapshot.productId()).isEqualTo(productId);
        assertThat(snapshot.at()).isEqualTo(T2);
        assertThat(snapshot.basePrice()).isEqualTo(usd(900));
        assertThat(snapshot.currentPrice()).isEqualTo(usd(700));
        assertThat(snapshot.discount()).isEqualTo(usd(200));
    }

    private static BigDecimal amount(int value) {
        return BigDecimal.valueOf(value);
    }

    private static Money usd(int value) {
        return Money.of(String.valueOf(value), "USD");
    }

    private static final class FakePriceRepository implements PriceRepository {

        private final Map<PriceId, Price> byId = new HashMap<>();

        @Override
        public Price save(Price price) {
            byId.put(price.getId(), price);
            return price;
        }

        @Override
        public Optional<Price> findById(PriceId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Price> findByProductAndVariant(ProductId productId, ProductVariantId variantId) {
            return byId.values().stream()
                    .filter(price -> price.getProductId().equals(productId))
                    .filter(price -> price.getVariantId().equals(Optional.ofNullable(variantId)))
                    .findFirst();
        }

        @Override
        public boolean existsByProductAndVariant(ProductId productId, ProductVariantId variantId) {
            return byId.values().stream()
                    .filter(price -> price.getProductId().equals(productId))
                    .anyMatch(price -> price.getVariantId().equals(Optional.ofNullable(variantId)));
        }
    }
}