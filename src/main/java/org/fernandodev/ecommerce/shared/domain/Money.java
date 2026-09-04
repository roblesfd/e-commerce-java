package org.fernandodev.ecommerce.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value object que representa una cantidad monetaria con su divisa.
 * <p>
 * Encapsula la escala (2 decimales con redondeo {@code HALF_UP}) y las
 * operaciones aritméticas básicas. Todas las operaciones entre dos valores
 * exigen la misma divisa.
 * </p>
 */
public final class Money {

    private static final int SCALE = 2;

    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        this.amount = Objects.requireNonNull(amount, "amount must not be null")
                .setScale(SCALE, RoundingMode.HALF_UP);
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
    }

    /**
     * Crea un {@link Money} a partir de un importe en texto y una divisa.
     */
    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    /**
     * Crea un {@link Money} a partir de un importe y una divisa.
     */
    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    /**
     * Crea un {@link Money} a partir de un importe y una divisa.
     */
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    /**
     * Devuelve un {@link Money} de importe cero en la divisa dada.
     */
    public static Money zero(String currencyCode) {
        return Money.of(BigDecimal.ZERO, currencyCode);
    }

    /**
     * Suma este valor monetario con otro de la misma divisa.
     *
     * @param other valor a sumar.
     * @return nueva cantidad resultante de la suma.
     * @throws IllegalArgumentException si las divisas difieren.
     */
    /**
     * Suma otro importe de la misma divisa.
     */
    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    /**
     * Resta otro valor monetario de esta cantidad.
     *
     * @param other valor a restar.
     * @return nueva cantidad resultante de la resta.
     * @throws IllegalArgumentException si las divisas difieren.
     */
    /**
     * Resta otro importe de la misma divisa.
     */
    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    /**
     * Multiplica esta cantidad por un factor.
     *
     * @param factor factor de multiplicación.
     * @return nueva cantidad resultante de la multiplicación.
     */
    /**
     * Multiplica el importe por un factor.
     */
    public Money multiply(BigDecimal factor) {
        return new Money(amount.multiply(Objects.requireNonNull(factor, "factor must not be null")), currency);
    }

    /**
     * Devuelve el importe negado.
     */
    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    /**
     * Indica si el importe es negativo o cero.
     */
    public boolean isNegativeOrZero() {
        return amount.signum() <= 0;
    }

    /**
     * Indica si el importe es positivo.
     */
    public boolean isPositive() {
        return amount.signum() > 0;
    }

    /**
     * Indica si el importe es cero.
     */
    public boolean isZero() {
        return amount.signum() == 0;
    }

    /**
     * Compara si esta cantidad es mayor que otra de la misma divisa.
     *
     * @param other valor a comparar.
     * @return {@code true} si esta cantidad es mayor.
     * @throws IllegalArgumentException si las divisas difieren.
     */
    /**
     * Indica si el importe es mayor que {@code other}.
     */
    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount) > 0;
    }

    /**
     * Compara si esta cantidad es menor que otra de la misma divisa.
     *
     * @param other valor a comparar.
     * @return {@code true} si esta cantidad es menor.
     * @throws IllegalArgumentException si las divisas difieren.
     */
    /**
     * Indica si el importe es menor que {@code other}.
     */
    public boolean isLessThan(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount) < 0;
    }

    /**
     * @return el importe con escala normalizada.
     */
    public BigDecimal amount() {
        return amount;
    }

    /**
     * @return la divisa.
     */
    public Currency currency() {
        return currency;
    }

    /**
     * @return el código ISO 4217 de la divisa.
     */
    public String currencyCode() {
        return currency.getCurrencyCode();
    }

    /**
     * Valida que la divisa coincida con {@code other}.
     */
    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + currency + " != " + other.currency);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money money)) {
            return false;
        }
        return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency.getCurrencyCode();
    }
}