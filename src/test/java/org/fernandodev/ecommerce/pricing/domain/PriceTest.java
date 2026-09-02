package org.fernandodev.ecommerce.pricing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceChange;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceOverride;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceOverrideId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceType;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class PriceTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2026-02-01T00:00:00Z");
    private static final Instant T3 = Instant.parse("2026-03-01T00:00:00Z");

    @Test
    void defineCreaPrecioBaseParaProducto() {
        Price price = Price.define(PriceId.generate(), ProductId.generate(), null, usd(900));

        assertThat(price.currentPrice(T1)).isEqualTo(usd(900));
        assertThat(price.getHistory()).isEmpty();
        assertThat(price.getOverrides()).isEmpty();
        assertThat(price.getVariantId()).isEmpty();
    }

    @Test
    void defineConImporteNoPositivoLanzaExcepcion() {
        assertThatThrownBy(() -> Price.define(PriceId.generate(), ProductId.generate(), null, usd(0)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void defineConVarianteAsignaVariantId() {
        ProductVariantId variantId = ProductVariantId.generate();

        Price price = Price.define(PriceId.generate(), ProductId.generate(), variantId, usd(900));

        assertThat(price.getVariantId()).contains(variantId);
    }

    @Test
    void updateBasePriceRegistraHistorialYCambiaElVigente() {
        Price price = base(900);

        price.updateBasePrice(usd(1000), T1);

        assertThat(price.currentPrice(T2)).isEqualTo(usd(1000));
        assertThat(price.getHistory()).containsExactly(new PriceChange(usd(900), T1));
    }

    @Test
    void updateConDivisaDistintaLanzaExcepcion() {
        Price price = base(900);

        assertThatThrownBy(() -> price.updateBasePrice(Money.of("1000", "MXN"), T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void currentPriceConOverrideActivoUsaElOverride() {
        Price price = base(900);
        price.addOverride(usd(700), T1, T3, PriceType.PROMOTIONAL);

        assertThat(price.currentPrice(T2)).isEqualTo(usd(700));
    }

    @Test
    void currentPriceFueraDeLaVentanaUsaElBase() {
        Price price = base(900);
        price.addOverride(usd(700), T2, T3, PriceType.PROMOTIONAL);

        assertThat(price.currentPrice(T1)).isEqualTo(usd(900));
        assertThat(price.currentPrice(T3)).isEqualTo(usd(900));
    }

    @Test
    void timeBasedOverrideSeAplicaEnSuVentana() {
        Price price = base(900);
        price.addOverride(usd(1200), T1, T2, PriceType.TIME_BASED);

        assertThat(price.currentPrice(T1.plusSeconds(1))).isEqualTo(usd(1200));
        assertThat(price.currentPrice(T3)).isEqualTo(usd(900));
    }

    @Test
    void addOverridePromocionalAceptadoSiEsMenorQueElBase() {
        Price price = base(900);

        PriceOverride override = price.addOverride(usd(700), T1, T2, PriceType.PROMOTIONAL);

        assertThat(price.getOverrides()).containsExactly(override);
    }

    @Test
    void addOverridePromocionalMayorOIgualQueBaseLanzaExcepcion() {
        Price price = base(900);

        assertThatThrownBy(() -> price.addOverride(usd(900), T1, T2, PriceType.PROMOTIONAL))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> price.addOverride(usd(1000), T1, T2, PriceType.PROMOTIONAL))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void addOverrideConRangoInvalidoLanzaExcepcion() {
        Price price = base(900);

        assertThatThrownBy(() -> price.addOverride(usd(700), T2, T1, PriceType.PROMOTIONAL))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void addOverrideSolapadoLanzaExcepcion() {
        Price price = base(900);
        price.addOverride(usd(700), T1, T3, PriceType.PROMOTIONAL);

        assertThatThrownBy(() -> price.addOverride(usd(600), T2, T3, PriceType.TIME_BASED))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void addOverrideConImporteNoPositivoLanzaExcepcion() {
        Price price = base(900);

        assertThatThrownBy(() -> price.addOverride(usd(0), T1, T2, PriceType.PROMOTIONAL))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void addOverrideConDivisaDistintaLanzaExcepcion() {
        Price price = base(900);

        assertThatThrownBy(() -> price.addOverride(Money.of("700", "MXN"), T1, T2, PriceType.PROMOTIONAL))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void removeOverrideQuitaElOverride() {
        Price price = base(900);
        PriceOverride override = price.addOverride(usd(700), T1, T2, PriceType.PROMOTIONAL);

        price.removeOverride(override.id());

        assertThat(price.getOverrides()).isEmpty();
        assertThat(price.currentPrice(T1)).isEqualTo(usd(900));
    }

    @Test
    void removeOverrideInexistenteLanzaExcepcion() {
        Price price = base(900);

        assertThatThrownBy(() -> price.removeOverride(PriceOverrideId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void discountConOverrideDevuelveElDescuento() {
        Price price = base(900);
        price.addOverride(usd(700), T1, T3, PriceType.PROMOTIONAL);

        assertThat(price.discountAt(T2)).isEqualTo(usd(200));
    }

    @Test
    void discountSinOverrideEsCero() {
        Price price = base(900);

        assertThat(price.discountAt(T1)).isEqualTo(usd(0));
    }

    private static Price base(int amount) {
        return Price.define(PriceId.generate(), ProductId.generate(), null, usd(amount));
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}