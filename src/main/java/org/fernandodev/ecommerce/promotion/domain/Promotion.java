package org.fernandodev.ecommerce.promotion.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.domain.value_object.CartContext;
import org.fernandodev.ecommerce.promotion.domain.value_object.CartItemLine;
import org.fernandodev.ecommerce.promotion.domain.value_object.CustomerUsage;
import org.fernandodev.ecommerce.promotion.domain.value_object.DiscountType;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.fernandodev.ecommerce.promotion.domain.value_object.VolumeTier;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz del contexto Promotions & Coupons que representa la regla de
 * descuento. La reglas de elegibilidad y el cálculo del descuento son lógica de
 * dominio pura, testable de forma independiente (spec §26).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Promotion {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private PromotionId id;
    private String name;
    private DiscountType type;
    private boolean active;
    private Instant validFrom;
    private Instant validTo;

    private BigDecimal percentageValue;
    private Money fixedAmount;
    @Getter(AccessLevel.NONE)
    private List<VolumeTier> volumeTiers;
    private int buyQuantity;
    private int getQuantity;

    private Money minPurchaseAmount;
    private Money maxDiscount;
    private int maxUsage;
    private int maxUsagePerCustomer;
    private int usedCount;

    @Getter(AccessLevel.NONE)
    private Set<ProductId> productIds;
    @Getter(AccessLevel.NONE)
    private Set<CategoryId> categoryIds;
    @Getter(AccessLevel.NONE)
    private List<CustomerUsage> usageByCustomer;

    /**
     * Factoría del agregado.
     */
    public static Promotion create(
            PromotionId id,
            String name,
            DiscountType type,
            Instant validFrom,
            Instant validTo,
            BigDecimal percentageValue,
            Money fixedAmount,
            List<VolumeTier> volumeTiers,
            int buyQuantity,
            int getQuantity,
            Money minPurchaseAmount,
            Money maxDiscount,
            int maxUsage,
            int maxUsagePerCustomer,
            Set<ProductId> productIds,
            Set<CategoryId> categoryIds
    ) {
        return crear(id, name, type, true, validFrom, validTo, percentageValue, fixedAmount,
                volumeTiers, buyQuantity, getQuantity, minPurchaseAmount, maxDiscount,
                maxUsage, maxUsagePerCustomer, 0, productIds, categoryIds, List.of());
    }

    /**
     * Factoría completa con copia defensiva e invariantes. Usada por la persistencia.
     */
    /**
     * Factoría del agregado.
     */
    public static Promotion crear(
            PromotionId id,
            String name,
            DiscountType type,
            boolean active,
            Instant validFrom,
            Instant validTo,
            BigDecimal percentageValue,
            Money fixedAmount,
            List<VolumeTier> volumeTiers,
            int buyQuantity,
            int getQuantity,
            Money minPurchaseAmount,
            Money maxDiscount,
            int maxUsage,
            int maxUsagePerCustomer,
            int usedCount,
            Set<ProductId> productIds,
            Set<CategoryId> categoryIds,
            List<CustomerUsage> usageByCustomer
    ) {
        Promotion promotion = new Promotion();
        promotion.id = Objects.requireNonNull(id, "id no puede ser null");
        promotion.name = requireName(name);
        promotion.type = Objects.requireNonNull(type, "type no puede ser null");
        promotion.active = active;
        Objects.requireNonNull(validFrom, "validFrom no puede ser null");
        Objects.requireNonNull(validTo, "validTo no puede ser null");
        if (!validFrom.isBefore(validTo)) {
            throw new BusinessRuleViolationException("validFrom debe ser anterior a validTo");
        }
        promotion.validFrom = validFrom;
        promotion.validTo = validTo;
        promotion.percentageValue = percentageValue;
        promotion.fixedAmount = fixedAmount;
        promotion.volumeTiers = new ArrayList<>(volumeTiers == null ? List.of() : volumeTiers);
        promotion.buyQuantity = buyQuantity;
        promotion.getQuantity = getQuantity;
        promotion.minPurchaseAmount = minPurchaseAmount;
        promotion.maxDiscount = maxDiscount;
        requireNonNegative(maxUsage, "maxUsage");
        requireNonNegative(maxUsagePerCustomer, "maxUsagePerCustomer");
        requireNonNegative(usedCount, "usedCount");
        promotion.maxUsage = maxUsage;
        promotion.maxUsagePerCustomer = maxUsagePerCustomer;
        promotion.usedCount = usedCount;
        promotion.productIds = new HashSet<>(productIds == null ? Set.of() : productIds);
        promotion.categoryIds = new HashSet<>(categoryIds == null ? Set.of() : categoryIds);
        promotion.usageByCustomer = new ArrayList<>(usageByCustomer == null ? List.of() : usageByCustomer);
        promotion.validateDiscountConfig();
        return promotion;
    }

    public Set<ProductId> getProductIds() {
        return Collections.unmodifiableSet(productIds);
    }

    public Set<CategoryId> getCategoryIds() {
        return Collections.unmodifiableSet(categoryIds);
    }

    public List<VolumeTier> getVolumeTiers() {
        return Collections.unmodifiableList(volumeTiers);
    }

    public List<CustomerUsage> getUsageByCustomer() {
        return Collections.unmodifiableList(usageByCustomer);
    }

    public boolean isScoped() {
        return !productIds.isEmpty() || !categoryIds.isEmpty();
    }

    public boolean isActiveAt(Instant at) {
        return active && !at.isBefore(validFrom) && at.isBefore(validTo);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void updateName(String name) {
        this.name = requireName(name);
    }

    public void setValidity(Instant validFrom, Instant validTo) {
        Objects.requireNonNull(validFrom, "validFrom no puede ser null");
        Objects.requireNonNull(validTo, "validTo no puede ser null");
        if (!validFrom.isBefore(validTo)) {
            throw new BusinessRuleViolationException("validFrom debe ser anterior a validTo");
        }
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public void setLimits(Money minPurchaseAmount, Money maxDiscount, int maxUsage, int maxUsagePerCustomer) {
        requireNonNegative(maxUsage, "maxUsage");
        requireNonNegative(maxUsagePerCustomer, "maxUsagePerCustomer");
        this.minPurchaseAmount = minPurchaseAmount;
        this.maxDiscount = maxDiscount;
        this.maxUsage = maxUsage;
        this.maxUsagePerCustomer = maxUsagePerCustomer;
    }

    public void addProduct(ProductId productId) {
        productIds.add(Objects.requireNonNull(productId, "productId no puede ser null"));
    }

    public void removeProduct(ProductId productId) {
        productIds.remove(productId);
    }

    public void addCategory(CategoryId categoryId) {
        categoryIds.add(Objects.requireNonNull(categoryId, "categoryId no puede ser null"));
    }

    public void removeCategory(CategoryId categoryId) {
        categoryIds.remove(categoryId);
    }

    /**
     * Indica por qué la promoción no es aplicable (vacío si sí es aplicable).
     */
    public Optional<String> ineligibilityReason(CartContext cart, UUID customerId, Instant at) {
        if (!isActiveAt(at)) {
            return Optional.of("La promoción no está activa o está fuera de su periodo");
        }
        if (maxUsage > 0 && usedCount >= maxUsage) {
            return Optional.of("Se alcanzó el límite de uso de la promoción");
        }
        if (maxUsagePerCustomer > 0 && customerUsage(customerId) >= maxUsagePerCustomer) {
            return Optional.of("Se alcanzó el límite de uso por cliente");
        }
        if (minPurchaseAmount != null && cart.subtotal().isLessThan(minPurchaseAmount)) {
            return Optional.of("No se alcanza el monto mínimo de compra");
        }
        if (isScoped() && !matchesScope(cart)) {
            return Optional.of("El carrito no cumple el alcance de la promoción");
        }
        return Optional.empty();
    }

    /**
     * Calcula el descuento para el carrito. Requiere haber validado la
     * elegibilidad previamente.
     */
    public Money computeDiscount(CartContext cart, Instant at) {
        Money subtotal = cart.subtotal();
        Money discount = switch (type) {
            case PERCENTAGE -> subtotal.multiply(percentageValue.divide(HUNDRED, 6, RoundingMode.HALF_UP));
            case FIXED_AMOUNT -> fixedAmount.isGreaterThan(subtotal) ? subtotal : fixedAmount;
            case VOLUME -> volumeDiscount(cart, subtotal);
            case BUY_X_GET_Y -> buyXGetYDiscount(cart);
        };
        if (discount.isGreaterThan(subtotal)) {
            discount = subtotal;
        }
        if (maxDiscount != null && discount.isGreaterThan(maxDiscount)) {
            discount = maxDiscount;
        }
        return discount;
    }

    /**
     * Registra un uso de la promoción validando los límites (total y por cliente).
     */
    public void recordUsage(UUID customerId) {
        Objects.requireNonNull(customerId, "customerId no puede ser null");
        if (maxUsage > 0 && usedCount >= maxUsage) {
            throw new BusinessRuleViolationException("Se alcanzó el límite de uso de la promoción");
        }
        if (maxUsagePerCustomer > 0 && customerUsage(customerId) >= maxUsagePerCustomer) {
            throw new BusinessRuleViolationException("Se alcanzó el límite de uso por cliente");
        }
        usedCount++;
        for (int i = 0; i < usageByCustomer.size(); i++) {
            CustomerUsage usage = usageByCustomer.get(i);
            if (usage.customerId().equals(customerId)) {
                usageByCustomer.set(i, new CustomerUsage(customerId, usage.count() + 1));
                return;
            }
        }
        usageByCustomer.add(new CustomerUsage(customerId, 1));
    }

    private int customerUsage(UUID customerId) {
        return usageByCustomer.stream()
                .filter(usage -> usage.customerId().equals(customerId))
                .map(CustomerUsage::count)
                .findFirst()
                .orElse(0);
    }

    private boolean matchesScope(CartContext cart) {
        return cart.items().stream().anyMatch(this::matchesScope);
    }

    private boolean matchesScope(CartItemLine item) {
        if (productIds.contains(item.productId())) {
            return true;
        }
        return item.categoryIds().stream().anyMatch(categoryIds::contains);
    }

    private List<CartItemLine> eligibleItems(CartContext cart) {
        if (!isScoped()) {
            return cart.items();
        }
        return cart.items().stream().filter(this::matchesScope).toList();
    }

    private Money volumeDiscount(CartContext cart, Money subtotal) {
        int eligibleQuantity = eligibleItems(cart).stream().mapToInt(CartItemLine::quantity).sum();
        VolumeTier tier = volumeTiers.stream()
                .filter(t -> eligibleQuantity >= t.minQuantity())
                .max((a, b) -> Integer.compare(a.minQuantity(), b.minQuantity()))
                .orElseThrow(() -> new BusinessRuleViolationException("No se alcanza ningún tramo de volumen"));
        return switch (tier.discountType()) {
            case PERCENTAGE -> subtotal.multiply(tier.value().divide(HUNDRED, 6, RoundingMode.HALF_UP));
            case FIXED_AMOUNT -> {
                Money tierFixed = Money.of(tier.value(), subtotal.currencyCode());
                yield tierFixed.isGreaterThan(subtotal) ? subtotal : tierFixed;
            }
            default -> throw new BusinessRuleViolationException("Tramo de volumen con tipo no soportado");
        };
    }

    private Money buyXGetYDiscount(CartContext cart) {
        Money discount = Money.zero(cart.subtotal().currencyCode());
        for (CartItemLine item : eligibleItems(cart)) {
            int groups = item.quantity() / (buyQuantity + getQuantity);
            if (groups > 0) {
                long freeUnits = (long) groups * getQuantity;
                discount = discount.add(item.unitPrice().multiply(BigDecimal.valueOf(freeUnits)));
            }
        }
        return discount;
    }

    private void validateDiscountConfig() {
        switch (type) {
            case PERCENTAGE -> {
                if (percentageValue == null || percentageValue.signum() <= 0 || percentageValue.compareTo(HUNDRED) > 0) {
                    throw new BusinessRuleViolationException("percentageValue debe estar entre 0 y 100");
                }
            }
            case FIXED_AMOUNT -> {
                if (fixedAmount == null || !fixedAmount.isPositive()) {
                    throw new BusinessRuleViolationException("fixedAmount debe ser un monto positivo");
                }
            }
            case VOLUME -> {
                if (volumeTiers.isEmpty()) {
                    throw new BusinessRuleViolationException("VOLUME requiere al menos un tramo");
                }
            }
            case BUY_X_GET_Y -> {
                if (buyQuantity <= 0 || getQuantity <= 0) {
                    throw new BusinessRuleViolationException("buyQuantity y getQuantity deben ser positivos");
                }
            }
        }
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("El nombre de la promoción es obligatorio");
        }
        if (name.length() > 120) {
            throw new BusinessRuleViolationException("El nombre de la promoción es demasiado largo");
        }
        return name;
    }

    private static void requireNonNegative(int value, String name) {
        if (value < 0) {
            throw new BusinessRuleViolationException(name + " no puede ser negativo");
        }
    }
}