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
 * Agregado raíz del contexto {@code Promotions &amp; Coupons} que representa una
 * promoción y las reglas asociadas a la aplicación de un descuento.
 *
 * <p>El agregado encapsula las reglas de elegibilidad, configuración del
 * descuento, límites de uso, alcance por producto o categoría y cálculo del
 * descuento aplicable a un carrito.</p>
 *
 * <p>La lógica de negocio se mantiene dentro del dominio para que pueda ser
 * ejecutada y probada de forma independiente de frameworks o mecanismos de
 * persistencia.</p>
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
     * Crea una nueva promoción con un estado inicial activo y sin usos
     * registrados.
     *
     * <p>Esta factoría representa la creación normal de una promoción desde
     * la aplicación. La configuración completa y las invariantes del agregado
     * son delegadas a la factoría interna {@link #crear}.</p>
     *
     * @param id identificador único de la promoción
     * @param name nombre de la promoción
     * @param type tipo de descuento que aplica la promoción
     * @param validFrom instante a partir del cual la promoción es válida
     * @param validTo instante a partir del cual la promoción deja de ser válida
     * @param percentageValue porcentaje de descuento, cuando corresponde
     * @param fixedAmount importe fijo de descuento, cuando corresponde
     * @param volumeTiers tramos de descuento por volumen, cuando corresponde
     * @param buyQuantity cantidad de unidades que el cliente debe comprar,
     *                    cuando corresponde a una promoción {@code BUY_X_GET_Y}
     * @param getQuantity cantidad de unidades gratuitas, cuando corresponde
     * @param minPurchaseAmount importe mínimo de compra requerido
     * @param maxDiscount descuento máximo permitido
     * @param maxUsage número máximo de usos globales; {@code 0} indica sin límite
     * @param maxUsagePerCustomer número máximo de usos por cliente;
     *                            {@code 0} indica sin límite
     * @param productIds productos a los que se limita la promoción
     * @param categoryIds categorías a las que se limita la promoción
     * @return nueva promoción configurada y validada
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
        return crear(
                id,
                name,
                type,
                true,
                validFrom,
                validTo,
                percentageValue,
                fixedAmount,
                volumeTiers,
                buyQuantity,
                getQuantity,
                minPurchaseAmount,
                maxDiscount,
                maxUsage,
                maxUsagePerCustomer,
                0,
                productIds,
                categoryIds,
                List.of()
        );
    }

    /**
     * Crea una promoción a partir de su estado completo.
     *
     * <p>Esta factoría realiza las validaciones de las invariantes del
     * agregado y crea copias defensivas de las colecciones recibidas para
     * evitar que el estado interno pueda ser modificado externamente.</p>
     *
     * <p>Se utiliza principalmente para reconstruir una promoción desde la
     * persistencia, ya que permite restaurar también su estado de activación,
     * contador de usos e historial de uso por cliente.</p>
     *
     * @param id identificador único de la promoción
     * @param name nombre de la promoción
     * @param type tipo de descuento
     * @param active indica si la promoción está activa
     * @param validFrom instante de inicio de vigencia
     * @param validTo instante de finalización de vigencia
     * @param percentageValue porcentaje de descuento configurado
     * @param fixedAmount importe fijo de descuento configurado
     * @param volumeTiers tramos de descuento por volumen
     * @param buyQuantity cantidad requerida para una promoción {@code BUY_X_GET_Y}
     * @param getQuantity cantidad gratuita para una promoción {@code BUY_X_GET_Y}
     * @param minPurchaseAmount importe mínimo requerido para aplicar la promoción
     * @param maxDiscount límite máximo del descuento
     * @param maxUsage límite global de usos
     * @param maxUsagePerCustomer límite de usos por cliente
     * @param usedCount cantidad de usos registrados
     * @param productIds identificadores de productos incluidos en el alcance
     * @param categoryIds identificadores de categorías incluidas en el alcance
     * @param usageByCustomer historial de usos agrupado por cliente
     * @return promoción reconstruida y validada
     * @throws NullPointerException si alguno de los valores obligatorios es
     *                              {@code null}
     * @throws BusinessRuleViolationException si se viola alguna invariante
     *                                        del agregado
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

        promotion.id = Objects.requireNonNull(
                id,
                "id no puede ser null"
        );

        promotion.name = requireName(name);

        promotion.type = Objects.requireNonNull(
                type,
                "type no puede ser null"
        );

        promotion.active = active;

        Objects.requireNonNull(
                validFrom,
                "validFrom no puede ser null"
        );

        Objects.requireNonNull(
                validTo,
                "validTo no puede ser null"
        );

        if (!validFrom.isBefore(validTo)) {
            throw new BusinessRuleViolationException(
                    "validFrom debe ser anterior a validTo"
            );
        }

        promotion.validFrom = validFrom;
        promotion.validTo = validTo;
        promotion.percentageValue = percentageValue;
        promotion.fixedAmount = fixedAmount;

        promotion.volumeTiers = new ArrayList<>(
                volumeTiers == null ? List.of() : volumeTiers
        );

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

        promotion.productIds = new HashSet<>(
                productIds == null ? Set.of() : productIds
        );

        promotion.categoryIds = new HashSet<>(
                categoryIds == null ? Set.of() : categoryIds
        );

        promotion.usageByCustomer = new ArrayList<>(
                usageByCustomer == null ? List.of() : usageByCustomer
        );

        promotion.validateDiscountConfig();

        return promotion;
    }

    /**
     * Obtiene los productos incluidos explícitamente en el alcance de la
     * promoción.
     *
     * <p>La colección retornada es de solo lectura para evitar modificaciones
     * directas sobre el estado interno del agregado.</p>
     *
     * @return conjunto no modificable de identificadores de productos
     */
    public Set<ProductId> getProductIds() {
        return Collections.unmodifiableSet(productIds);
    }

    /**
     * Obtiene las categorías incluidas explícitamente en el alcance de la
     * promoción.
     *
     * <p>La colección retornada es de solo lectura para preservar la
     * encapsulación del agregado.</p>
     *
     * @return conjunto no modificable de identificadores de categorías
     */
    public Set<CategoryId> getCategoryIds() {
        return Collections.unmodifiableSet(categoryIds);
    }

    /**
     * Obtiene los tramos configurados para una promoción basada en volumen.
     *
     * @return lista no modificable de tramos de volumen
     */
    public List<VolumeTier> getVolumeTiers() {
        return Collections.unmodifiableList(volumeTiers);
    }

    /**
     * Obtiene el registro de usos de la promoción agrupado por cliente.
     *
     * @return lista no modificable de usos registrados por cliente
     */
    public List<CustomerUsage> getUsageByCustomer() {
        return Collections.unmodifiableList(usageByCustomer);
    }

    /**
     * Determina si la promoción tiene un alcance específico.
     *
     * <p>Una promoción se considera acotada cuando contiene al menos un
     * producto o una categoría explícitamente configurados. Si no tiene
     * ninguno, se considera aplicable de forma general.</p>
     *
     * @return {@code true} si la promoción está limitada por productos o
     *         categorías; {@code false} si no tiene un alcance específico
     */
    public boolean isScoped() {
        return !productIds.isEmpty() || !categoryIds.isEmpty();
    }

    /**
     * Determina si la promoción está activa y es válida en el instante
     * especificado.
     *
     * <p>El inicio de vigencia es inclusivo y el final de vigencia es
     * exclusivo, es decir:
     * {@code validFrom <= at < validTo}.</p>
     *
     * @param at instante utilizado como referencia para evaluar la vigencia
     * @return {@code true} si la promoción está activa y dentro de su período
     *         de vigencia; {@code false} en caso contrario
     */
    public boolean isActiveAt(Instant at) {
        return active
                && !at.isBefore(validFrom)
                && at.isBefore(validTo);
    }

    /**
     * Activa la promoción.
     *
     * <p>Una promoción activada puede considerarse elegible siempre que
     * también se encuentre dentro de su período de vigencia y cumpla las
     * demás reglas de elegibilidad.</p>
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Desactiva la promoción.
     *
     * <p>Una promoción desactivada no puede considerarse elegible,
     * independientemente de su período de vigencia.</p>
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Actualiza el nombre de la promoción.
     *
     * <p>El nuevo nombre se valida antes de modificar el estado del agregado.</p>
     *
     * @param name nuevo nombre de la promoción
     * @throws BusinessRuleViolationException si el nombre es nulo, está vacío,
     *                                        contiene únicamente espacios o
     *                                        supera la longitud permitida
     */
    public void updateName(String name) {
        this.name = requireName(name);
    }

    /**
     * Actualiza el período de vigencia de la promoción.
     *
     * <p>El instante inicial debe ser estrictamente anterior al instante
     * final.</p>
     *
     * @param validFrom nuevo instante de inicio de vigencia
     * @param validTo nuevo instante de finalización de vigencia
     * @throws NullPointerException si alguno de los instantes es {@code null}
     * @throws BusinessRuleViolationException si {@code validFrom} no es
     *                                        anterior a {@code validTo}
     */
    public void setValidity(Instant validFrom, Instant validTo) {
        Objects.requireNonNull(
                validFrom,
                "validFrom no puede ser null"
        );

        Objects.requireNonNull(
                validTo,
                "validTo no puede ser null"
        );

        if (!validFrom.isBefore(validTo)) {
            throw new BusinessRuleViolationException(
                    "validFrom debe ser anterior a validTo"
            );
        }

        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    /**
     * Actualiza los límites de aplicación de la promoción.
     *
     * <p>Los límites de uso no pueden ser negativos. Un valor de {@code 0}
     * representa ausencia de límite.</p>
     *
     * @param minPurchaseAmount importe mínimo de compra requerido
     * @param maxDiscount importe máximo de descuento permitido
     * @param maxUsage límite global de usos
     * @param maxUsagePerCustomer límite de usos por cliente
     * @throws BusinessRuleViolationException si alguno de los límites de uso
     *                                        es negativo
     */
    public void setLimits(
            Money minPurchaseAmount,
            Money maxDiscount,
            int maxUsage,
            int maxUsagePerCustomer) {

        requireNonNegative(maxUsage, "maxUsage");
        requireNonNegative(
                maxUsagePerCustomer,
                "maxUsagePerCustomer"
        );

        this.minPurchaseAmount = minPurchaseAmount;
        this.maxDiscount = maxDiscount;
        this.maxUsage = maxUsage;
        this.maxUsagePerCustomer = maxUsagePerCustomer;
    }

    /**
     * Agrega un producto al alcance de la promoción.
     *
     * @param productId identificador del producto que se incluirá
     * @throws NullPointerException si {@code productId} es {@code null}
     */
    public void addProduct(ProductId productId) {
        productIds.add(
                Objects.requireNonNull(
                        productId,
                        "productId no puede ser null"
                )
        );
    }

    /**
     * Elimina un producto del alcance de la promoción.
     *
     * <p>Si el producto no pertenece al alcance actual de la promoción,
     * no se realiza ninguna modificación.</p>
     *
     * @param productId identificador del producto que se eliminará
     */
    public void removeProduct(ProductId productId) {
        productIds.remove(productId);
    }

    /**
     * Agrega una categoría al alcance de la promoción.
     *
     * @param categoryId identificador de la categoría que se incluirá
     * @throws NullPointerException si {@code categoryId} es {@code null}
     */
    public void addCategory(CategoryId categoryId) {
        categoryIds.add(
                Objects.requireNonNull(
                        categoryId,
                        "categoryId no puede ser null"
                )
        );
    }

    /**
     * Elimina una categoría del alcance de la promoción.
     *
     * <p>Si la categoría no pertenece al alcance actual de la promoción,
     * no se realiza ninguna modificación.</p>
     *
     * @param categoryId identificador de la categoría que se eliminará
     */
    public void removeCategory(CategoryId categoryId) {
        categoryIds.remove(categoryId);
    }

    /**
     * Determina por qué una promoción no es elegible para un carrito.
     *
     * <p>La evaluación considera el estado y período de vigencia de la
     * promoción, los límites globales y por cliente, el importe mínimo de
     * compra y el alcance configurado por producto o categoría.</p>
     *
     * <p>Cuando todas las reglas de elegibilidad se cumplen, se devuelve
     * {@link Optional#empty()}.</p>
     *
     * @param cart contexto del carrito sobre el que se evalúa la promoción
     * @param customerId identificador del cliente que realiza la compra
     * @param at instante utilizado como referencia para evaluar la vigencia
     * @return {@link Optional} que contiene la razón por la que la promoción
     *         no es aplicable; vacío cuando la promoción es elegible
     */
    public Optional<String> ineligibilityReason(
            CartContext cart,
            UUID customerId,
            Instant at) {

        if (!isActiveAt(at)) {
            return Optional.of(
                    "La promoción no está activa o está fuera de su periodo"
            );
        }

        if (maxUsage > 0 && usedCount >= maxUsage) {
            return Optional.of(
                    "Se alcanzó el límite de uso de la promoción"
            );
        }

        if (maxUsagePerCustomer > 0
                && customerUsage(customerId) >= maxUsagePerCustomer) {

            return Optional.of(
                    "Se alcanzó el límite de uso por cliente"
            );
        }

        if (minPurchaseAmount != null
                && cart.subtotal().isLessThan(minPurchaseAmount)) {

            return Optional.of(
                    "No se alcanza el monto mínimo de compra"
            );
        }

        if (isScoped() && !matchesScope(cart)) {
            return Optional.of(
                    "El carrito no cumple el alcance de la promoción"
            );
        }

        return Optional.empty();
    }

    /**
     * Calcula el importe del descuento que corresponde aplicar al carrito.
     *
     * <p>El cálculo se realiza de acuerdo con el tipo de descuento configurado:
     * porcentaje, importe fijo, volumen o {@code BUY_X_GET_Y}.</p>
     *
     * <p>El descuento resultante nunca puede superar el subtotal del carrito.
     * Cuando existe un límite máximo mediante {@code maxDiscount}, también
     * se aplica dicho límite.</p>
     *
     * <p>Este método asume que la promoción ya fue validada como elegible.</p>
     *
     * @param cart contexto del carrito sobre el que se calculará el descuento
     * @param at instante de referencia del cálculo
     * @return importe del descuento que debe aplicarse al carrito
     * @throws BusinessRuleViolationException si la configuración de la
     *                                        promoción no permite calcular
     *                                        el descuento
     */
    public Money computeDiscount(CartContext cart, Instant at) {
        Money subtotal = cart.subtotal();

        Money discount = switch (type) {
            case PERCENTAGE ->
                    subtotal.multiply(
                            percentageValue.divide(
                                    HUNDRED,
                                    6,
                                    RoundingMode.HALF_UP
                            )
                    );

            case FIXED_AMOUNT ->
                    fixedAmount.isGreaterThan(subtotal)
                            ? subtotal
                            : fixedAmount;

            case VOLUME ->
                    volumeDiscount(cart, subtotal);

            case BUY_X_GET_Y ->
                    buyXGetYDiscount(cart);
        };

        if (discount.isGreaterThan(subtotal)) {
            discount = subtotal;
        }

        if (maxDiscount != null
                && discount.isGreaterThan(maxDiscount)) {

            discount = maxDiscount;
        }

        return discount;
    }

    /**
     * Registra un nuevo uso de la promoción para un cliente.
     *
     * <p>Incrementa el contador global de usos y actualiza el contador
     * correspondiente al cliente.</p>
     *
     * <p>Antes de modificar el estado del agregado se validan tanto el límite
     * global como el límite específico por cliente.</p>
     *
     * @param customerId identificador del cliente que utiliza la promoción
     * @throws NullPointerException si {@code customerId} es {@code null}
     * @throws BusinessRuleViolationException si se alcanzó el límite global
     *                                        o el límite por cliente
     */
    public void recordUsage(UUID customerId) {
        Objects.requireNonNull(
                customerId,
                "customerId no puede ser null"
        );

        if (maxUsage > 0 && usedCount >= maxUsage) {
            throw new BusinessRuleViolationException(
                    "Se alcanzó el límite de uso de la promoción"
            );
        }

        if (maxUsagePerCustomer > 0
                && customerUsage(customerId) >= maxUsagePerCustomer) {

            throw new BusinessRuleViolationException(
                    "Se alcanzó el límite de uso por cliente"
            );
        }

        usedCount++;

        for (int i = 0; i < usageByCustomer.size(); i++) {
            CustomerUsage usage = usageByCustomer.get(i);

            if (usage.customerId().equals(customerId)) {
                usageByCustomer.set(
                        i,
                        new CustomerUsage(
                                customerId,
                                usage.count() + 1
                        )
                );
                return;
            }
        }

        usageByCustomer.add(
                new CustomerUsage(customerId, 1)
        );
    }

    /**
     * Obtiene la cantidad de veces que un cliente ha utilizado la promoción.
     *
     * @param customerId identificador del cliente
     * @return cantidad de usos registrados para el cliente; {@code 0} si no
     *         existe un registro para dicho cliente
     */
    private int customerUsage(UUID customerId) {
        return usageByCustomer.stream()
                .filter(usage -> usage.customerId().equals(customerId))
                .map(CustomerUsage::count)
                .findFirst()
                .orElse(0);
    }

    /**
     * Determina si al menos una línea del carrito pertenece al alcance
     * configurado para la promoción.
     *
     * @param cart contexto del carrito que será evaluado
     * @return {@code true} si al menos una línea coincide con el alcance;
     *         {@code false} en caso contrario
     */
    private boolean matchesScope(CartContext cart) {
        return cart.items()
                .stream()
                .anyMatch(this::matchesScope);
    }

    /**
     * Determina si una línea específica del carrito pertenece al alcance
     * de la promoción.
     *
     * <p>Una línea coincide cuando su producto está incluido directamente
     * en la promoción o cuando alguna de sus categorías pertenece a las
     * categorías configuradas.</p>
     *
     * @param item línea del carrito que será evaluada
     * @return {@code true} si la línea pertenece al alcance de la promoción
     */
    private boolean matchesScope(CartItemLine item) {
        if (productIds.contains(item.productId())) {
            return true;
        }

        return item.categoryIds()
                .stream()
                .anyMatch(categoryIds::contains);
    }

    /**
     * Obtiene las líneas del carrito que son elegibles para la promoción.
     *
     * <p>Si la promoción no tiene un alcance específico, todas las líneas del
     * carrito son elegibles. En caso contrario, únicamente se incluyen las
     * líneas que coinciden con los productos o categorías configurados.</p>
     *
     * @param cart contexto del carrito
     * @return líneas del carrito que pueden participar en el cálculo
     *         del descuento
     */
    private List<CartItemLine> eligibleItems(CartContext cart) {
        if (!isScoped()) {
            return cart.items();
        }

        return cart.items()
                .stream()
                .filter(this::matchesScope)
                .toList();
    }

    /**
     * Calcula el descuento correspondiente a una promoción basada en
     * volumen.
     *
     * <p>Primero determina la cantidad total de unidades elegibles y después
     * selecciona el tramo con la mayor cantidad mínima que pueda aplicarse.</p>
     *
     * <p>El descuento del tramo puede ser porcentual o de importe fijo.</p>
     *
     * @param cart contexto del carrito
     * @param subtotal subtotal del carrito utilizado como base para el cálculo
     * @return importe del descuento correspondiente al tramo alcanzado
     * @throws BusinessRuleViolationException si no se alcanza ningún tramo
     *                                        configurado
     */
    private Money volumeDiscount(
            CartContext cart,
            Money subtotal) {

        int eligibleQuantity = eligibleItems(cart)
                .stream()
                .mapToInt(CartItemLine::quantity)
                .sum();

        VolumeTier tier = volumeTiers
                .stream()
                .filter(t -> eligibleQuantity >= t.minQuantity())
                .max((a, b) ->
                        Integer.compare(
                                a.minQuantity(),
                                b.minQuantity()
                        )
                )
                .orElseThrow(() ->
                        new BusinessRuleViolationException(
                                "No se alcanza ningún tramo de volumen"
                        )
                );

        return switch (tier.discountType()) {

            case PERCENTAGE ->
                    subtotal.multiply(
                            tier.value().divide(
                                    HUNDRED,
                                    6,
                                    RoundingMode.HALF_UP
                            )
                    );

            case FIXED_AMOUNT -> {
                Money tierFixed = Money.of(
                        tier.value(),
                        subtotal.currencyCode()
                );

                yield tierFixed.isGreaterThan(subtotal)
                        ? subtotal
                        : tierFixed;
            }

            default ->
                    throw new BusinessRuleViolationException(
                            "Tramo de volumen con tipo no soportado"
                    );
        };
    }

    /**
     * Calcula el descuento correspondiente a una promoción
     * {@code BUY_X_GET_Y}.
     *
     * <p>Para cada línea elegible determina cuántos grupos completos de
     * {@code buyQuantity + getQuantity} pueden formarse y calcula como
     * descuento el precio correspondiente a las unidades gratuitas.</p>
     *
     * @param cart contexto del carrito
     * @return importe total del descuento calculado
     */
    private Money buyXGetYDiscount(CartContext cart) {
        Money discount = Money.zero(
                cart.subtotal().currencyCode()
        );

        for (CartItemLine item : eligibleItems(cart)) {

            int groups = item.quantity()
                    / (buyQuantity + getQuantity);

            if (groups > 0) {
                long freeUnits = (long) groups * getQuantity;

                discount = discount.add(
                        item.unitPrice().multiply(
                                BigDecimal.valueOf(freeUnits)
                        )
                );
            }
        }

        return discount;
    }

    /**
     * Valida que la configuración del descuento sea coherente con el tipo
     * de promoción.
     *
     * <p>Cada tipo de descuento requiere una configuración específica:</p>
     *
     * <ul>
     *     <li>{@code PERCENTAGE}: requiere un porcentaje mayor que cero
     *     y menor o igual a 100.</li>
     *     <li>{@code FIXED_AMOUNT}: requiere un importe positivo.</li>
     *     <li>{@code VOLUME}: requiere al menos un tramo de volumen.</li>
     *     <li>{@code BUY_X_GET_Y}: requiere cantidades positivas tanto
     *     para comprar como para recibir.</li>
     * </ul>
     *
     * @throws BusinessRuleViolationException si la configuración no es válida
     *                                        para el tipo de descuento
     */
    private void validateDiscountConfig() {
        switch (type) {

            case PERCENTAGE -> {
                if (percentageValue == null
                        || percentageValue.signum() <= 0
                        || percentageValue.compareTo(HUNDRED) > 0) {

                    throw new BusinessRuleViolationException(
                            "percentageValue debe estar entre 0 y 100"
                    );
                }
            }

            case FIXED_AMOUNT -> {
                if (fixedAmount == null
                        || !fixedAmount.isPositive()) {

                    throw new BusinessRuleViolationException(
                            "fixedAmount debe ser un monto positivo"
                    );
                }
            }

            case VOLUME -> {
                if (volumeTiers.isEmpty()) {
                    throw new BusinessRuleViolationException(
                            "VOLUME requiere al menos un tramo"
                    );
                }
            }

            case BUY_X_GET_Y -> {
                if (buyQuantity <= 0 || getQuantity <= 0) {
                    throw new BusinessRuleViolationException(
                            "buyQuantity y getQuantity deben ser positivos"
                    );
                }
            }
        }
    }

    /**
     * Valida el nombre de la promoción.
     *
     * <p>El nombre es obligatorio, no puede estar compuesto únicamente por
     * espacios y no puede superar los 120 caracteres.</p>
     *
     * @param name nombre que será validado
     * @return el mismo nombre una vez validado
     * @throws BusinessRuleViolationException si el nombre no cumple las
     *                                        restricciones del dominio
     */
    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException(
                    "El nombre de la promoción es obligatorio"
            );
        }

        if (name.length() > 120) {
            throw new BusinessRuleViolationException(
                    "El nombre de la promoción es demasiado largo"
            );
        }

        return name;
    }

    /**
     * Valida que un valor entero utilizado como límite o contador no sea
     * negativo.
     *
     * @param value valor que será validado
     * @param name nombre del campo utilizado para construir el mensaje
     *             de error
     * @throws BusinessRuleViolationException si {@code value} es negativo
     */
    private static void requireNonNegative(
            int value,
            String name) {

        if (value < 0) {
            throw new BusinessRuleViolationException(
                    name + " no puede ser negativo"
            );
        }
    }
}
