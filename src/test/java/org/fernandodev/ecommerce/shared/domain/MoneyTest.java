package org.fernandodev.ecommerce.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void sumaMantieneDivisaYEscala() {
        Money total = Money.of("10.50", "USD").add(Money.of("5.25", "USD"));

        assertThat(total.amount()).isEqualByComparingTo("15.75");
        assertThat(total.currencyCode()).isEqualTo("USD");
    }

    @Test
    void restaPuedeDejarValorNegativo() {
        Money resultado = Money.of("5.00", "EUR").subtract(Money.of("10.00", "EUR"));

        assertThat(resultado.amount()).isEqualByComparingTo("-5.00");
        assertThat(resultado.isNegativeOrZero()).isTrue();
    }

    @Test
    void multiplicacionRedondeaADosDecimales() {
        Money resultado = Money.of("1.99", "USD").multiply(new BigDecimal("3"));

        assertThat(resultado.amount()).isEqualByComparingTo("5.97");
    }

    @Test
    void zeroCreaCantidadNula() {
        Money zero = Money.zero("USD");

        assertThat(zero.amount()).isEqualByComparingTo("0.00");
        assertThat(zero.isZero()).isTrue();
        assertThat(zero.isNegativeOrZero()).isTrue();
    }

    @Test
    void operacionesConDivisasDistintasSonRechazadas() {
        Money usd = Money.of("1.00", "USD");
        Money eur = Money.of("1.00", "EUR");

        assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
        assertThatThrownBy(() -> usd.isGreaterThan(eur))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void divisaInvalidaEsRechazada() {
        assertThatThrownBy(() -> Money.of("1.00", "NO_EXISTE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalityComparaPorCantidadYDivisa() {
        assertThat(Money.of("2.00", "USD")).isEqualTo(Money.of("2.00", "USD"));
        assertThat(Money.of("2.00", "USD")).isNotEqualTo(Money.of("2.00", "EUR"));
        assertThat(Money.of("2.0", "USD")).isEqualTo(Money.of("2.00", "USD"));
    }
}