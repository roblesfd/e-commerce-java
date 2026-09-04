package org.fernandodev.ecommerce.pricing.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz del contexto Pricing que representa el precio aplicable a un
 * producto o a una variante de producto.
 * <p>
 * Mantiene el precio base, su historial de cambios y los overrides
 * (promocionales o por tiempo). El precio vigente en un instante es el override
 * aplicable en ese instante o, si no, el precio base (spec §23).
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Price {

    private PriceId id;
    private ProductId productId;
    private ProductVariantId variantId;
    private Money basePrice;
    @Getter(AccessLevel.NONE)
    private List<PriceChange> history;
    @Getter(AccessLevel.NONE)
    private List<PriceOverride> overrides;

    /**
     * Define el precio base de un producto o variante.
     */
    /**
     * Define el precio base de un producto o variante.
     *
     * @param id        identificador del precio.
     * @param productId producto.
     * @param variantId variante (null si es a nivel producto).
     * @param basePrice precio base.
     * @return el precio definido.
     * @throws BusinessRuleViolationException si el precio base no es positivo.
     */
    public static Price define(PriceId id, ProductId productId, ProductVariantId variantId, Money basePrice) {
        return crear(id, productId, variantId, basePrice, List.of(), List.of());
    }

    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     */
    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     *
     * @return el precio reconstruido.
     */
    public static Price crear(
            PriceId id,
            ProductId productId,
            ProductVariantId variantId,
            Money basePrice,
            List<PriceChange> history,
            List<PriceOverride> overrides
    ) {
        Price price = new Price();
        price.id = Objects.requireNonNull(id, "id no puede ser null");
        price.productId = Objects.requireNonNull(productId, "productId no puede ser null");
        price.variantId = variantId;
        price.basePrice = Objects.requireNonNull(basePrice, "basePrice no puede ser null");
        requirePositive(price.basePrice);
        price.history = new ArrayList<>(history == null ? List.of() : history);
        price.overrides = new ArrayList<>(overrides == null ? List.of() : overrides);
        return price;
    }

    /**
     * @return la variante, o {@code Optional.empty()} si es a nivel producto.
     */
    public Optional<ProductVariantId> getVariantId() {
        return Optional.ofNullable(variantId);
    }

    /**
     * @return el historial de cambios del precio base como lista inmutable.
     */
    public List<PriceChange> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * @return los overrides del precio como lista inmutable.
     */
    public List<PriceOverride> getOverrides() {
        return Collections.unmodifiableList(overrides);
    }

    /**
     * Actualiza el precio base registrando el valor anterior en el historial.
     */
    /**
     * Actualiza el precio base registrando el valor anterior en el historial.
     *
     * @param newPrice nuevo precio base.
     * @param at       momento del cambio.
     * @throws BusinessRuleViolationException si el precio no es positivo o la divisa difiere.
     */
    public void updateBasePrice(Money newPrice, Instant at) {
        Objects.requireNonNull(newPrice, "newPrice no puede ser null");
        Objects.requireNonNull(at, "at no puede ser null");
        requirePositive(newPrice);
        requireSameCurrency(newPrice);
        history.add(new PriceChange(basePrice, at));
        basePrice = newPrice;
    }

    /**
     * Añade un override (promocional o por tiempo) validando las reglas de
     * negocio: importe positivo, divisa consistente, rango válido, sin
     * solapamiento y, si es promocional, menor que el precio base.
     *
     * @return el override creado.
     */
    /**
     * Añade un override (promocional o por tiempo) validando las reglas de
     * negocio: importe positivo, divisa consistente, rango válido, sin
     * solapamiento y, si es promocional, menor que el precio base.
     *
     * @param amount    importe del override.
     * @param validFrom inicio de la ventana.
     * @param validTo   fin de la ventana.
     * @param type      tipo de override.
     * @return el override creado.
     * @throws BusinessRuleViolationException si alguna regla de negocio se incumple.
     */
    public PriceOverride addOverride(Money amount, Instant validFrom, Instant validTo, PriceType type) {
        Objects.requireNonNull(amount, "amount no puede ser null");
        Objects.requireNonNull(validFrom, "validFrom no puede ser null");
        Objects.requireNonNull(validTo, "validTo no puede ser null");
        Objects.requireNonNull(type, "type no puede ser null");
        requirePositive(amount);
        requireSameCurrency(amount);
        if (!validFrom.isBefore(validTo)) {
            throw new BusinessRuleViolationException("validFrom debe ser anterior a validTo");
        }
        if (type == PriceType.PROMOTIONAL && !amount.isLessThan(basePrice)) {
            throw new BusinessRuleViolationException("El precio promocional debe ser menor que el precio base");
        }
        boolean solapa = overrides.stream().anyMatch(existing -> existing.overlaps(validFrom, validTo));
        if (solapa) {
            throw new BusinessRuleViolationException("El override se solapa con uno ya existente");
        }
        PriceOverride override = new PriceOverride(PriceOverrideId.generate(), amount, validFrom, validTo, type);
        overrides.add(override);
        return override;
    }

    /**
     * Elimina un override por identificador.
     *
     * @param id identificador del override.
     * @throws ResourceNotFoundException si el override no existe.
     */
    public void removeOverride(PriceOverrideId id) {
        Objects.requireNonNull(id, "id no puede ser null");
        for (int i = 0; i < overrides.size(); i++) {
            if (overrides.get(i).id().equals(id)) {
                overrides.remove(i);
                return;
            }
        }
        throw new ResourceNotFoundException("Override no encontrado: " + id);
    }

    /**
     * @param at instante.
     * @return el override aplicable en {@code at}, o {@code Optional.empty()} si no hay.
     */
    public Optional<PriceOverride> applicableOverride(Instant at) {
        Objects.requireNonNull(at, "at no puede ser null");
        return overrides.stream()
                .filter(override -> !at.isBefore(override.validFrom()) && at.isBefore(override.validTo()))
                .findFirst();
    }

    /**
     * Precio vigente en el instante dado.
     */
    /**
     * Precio vigente en el instante dado (override aplicable o precio base).
     *
     * @param at instante.
     * @return el precio vigente.
     */
    public Money currentPrice(Instant at) {
        return applicableOverride(at).map(PriceOverride::amount).orElse(basePrice);
    }

    /**
     * Descuento aplicado en el instante dado: precio base menos precio vigente.
     */
    /**
     * Descuento aplicado en el instante dado: precio base menos precio vigente.
     *
     * @param at instante.
     * @return el descuento.
     */
    public Money discountAt(Instant at) {
        return basePrice.subtract(currentPrice(at));
    }

    /**
     * Valida que el importe sea positivo.
     *
     * @param money importe a validar.
     * @throws BusinessRuleViolationException si no es positivo.
     */
    private static void requirePositive(Money money) {
        if (!money.isPositive()) {
            throw new BusinessRuleViolationException("El importe debe ser positivo");
        }
    }

    /**
     * Valida que la divisa coincida con la del precio base.
     *
     * @param money importe a validar.
     * @throws BusinessRuleViolationException si la divisa difiere.
     */
    private void requireSameCurrency(Money money) {
        if (!money.currency().equals(basePrice.currency())) {
            throw new BusinessRuleViolationException(
                    "La divisa debe coincidir con la del precio base: " + basePrice.currencyCode());
        }
    }
}