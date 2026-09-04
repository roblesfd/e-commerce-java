package org.fernandodev.ecommerce.returns.domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnPolicyId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa una política de devoluciones configurable.
 *
 * <p>La política define el periodo máximo durante el cual pueden realizarse
 * devoluciones y, opcionalmente, los productos que pueden ser reembolsados.</p>
 *
 * <p>Una política puede encontrarse activa o inactiva. Cuando no se especifican
 * productos restringidos, todos los productos se consideran reembolsables.</p>
 *
 * <p>El agregado garantiza que la ventana de devolución sea siempre un valor
 * positivo.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReturnPolicy {

    /**
     * Identificador único de la política de devoluciones.
     */
    private ReturnPolicyId id;

    /**
     * Número máximo de días durante los cuales puede solicitarse una devolución.
     */
    private int returnWindowDays;

    /**
     * Indica si la política se encuentra actualmente activa.
     */
    private boolean active;

    /**
     * Identificadores de los productos que pueden ser reembolsados.
     *
     * <p>Una colección vacía indica que la política no restringe la devolución
     * a productos específicos y, por tanto, cualquier producto puede ser
     * considerado reembolsable.</p>
     *
     * <p>El acceso mediante Lombok se deshabilita para este atributo porque
     * el agregado expone una colección de solo lectura a través de
     * {@link #getReturnableProductIds()}.</p>
     */
    @Getter(AccessLevel.NONE)
    private Set<ProductId> returnableProductIds;

    /**
     * Crea una nueva política de devoluciones activa.
     *
     * <p>La política se crea inicialmente activa y con la ventana de
     * devolución especificada.</p>
     *
     * @param id identificador único de la política.
     * @param returnWindowDays número de días permitidos para solicitar
     *                         una devolución.
     * @param returnableProductIds identificadores de los productos que
     *                              pueden ser reembolsados. Una colección
     *                              vacía permite cualquier producto.
     * @return una nueva instancia de {@code ReturnPolicy}.
     * @throws NullPointerException si {@code id} es {@code null}.
     * @throws BusinessRuleViolationException si {@code returnWindowDays}
     *         es menor o igual que cero.
     */
    public static ReturnPolicy create(
            ReturnPolicyId id,
            int returnWindowDays,
            Set<ProductId> returnableProductIds
    ) {
        return crear(
                id,
                returnWindowDays,
                true,
                returnableProductIds
        );
    }

    /**
     * Crea una nueva política de devoluciones con el estado indicado.
     *
     * <p>Este método centraliza la creación del agregado y garantiza que
     * la ventana de devolución cumpla las reglas de negocio.</p>
     *
     * @param id identificador único de la política.
     * @param returnWindowDays número de días permitidos para solicitar
     *                         una devolución.
     * @param active indica si la política debe crearse activa.
     * @param returnableProductIds identificadores de los productos que
     *                              pueden ser reembolsados. Una colección
     *                              vacía permite cualquier producto.
     * @return una nueva instancia de {@code ReturnPolicy}.
     * @throws NullPointerException si {@code id} es {@code null}.
     * @throws BusinessRuleViolationException si {@code returnWindowDays}
     *         es menor o igual que cero.
     */
    public static ReturnPolicy crear(
            ReturnPolicyId id,
            int returnWindowDays,
            boolean active,
            Set<ProductId> returnableProductIds
    ) {
        ReturnPolicy policy = new ReturnPolicy();

        policy.id = Objects.requireNonNull(
                id,
                "id no puede ser null"
        );

        if (returnWindowDays <= 0) {
            throw new BusinessRuleViolationException(
                    "returnWindowDays debe ser positivo"
            );
        }

        policy.returnWindowDays = returnWindowDays;
        policy.active = active;
        policy.returnableProductIds = new HashSet<>(
                returnableProductIds == null
                        ? Set.of()
                        : returnableProductIds
        );

        return policy;
    }

    /**
     * Obtiene los identificadores de los productos que pueden ser
     * reembolsados por esta política.
     *
     * <p>La colección devuelta es de solo lectura, evitando que código
     * externo modifique directamente el estado interno del agregado.</p>
     *
     * <p>Una colección vacía indica que la política no restringe la
     * devolución a productos específicos.</p>
     *
     * @return conjunto no modificable de identificadores de productos
     *         reembolsables.
     */
    public Set<ProductId> getReturnableProductIds() {
        return java.util.Collections.unmodifiableSet(
                returnableProductIds
        );
    }

    /**
     * Activa la política de devoluciones.
     *
     * <p>Una política activa puede utilizarse para determinar si una
     * devolución cumple las condiciones establecidas.</p>
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Desactiva la política de devoluciones.
     *
     * <p>Una política desactivada deja de estar disponible para determinar
     * la elegibilidad de nuevas devoluciones.</p>
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Determina si un producto puede ser reembolsado de acuerdo con esta
     * política.
     *
     * <p>Cuando la política no contiene productos específicos, cualquier
     * producto se considera reembolsable. En caso contrario, únicamente
     * los productos incluidos en la lista son considerados reembolsables.</p>
     *
     * @param productId identificador del producto que se desea comprobar.
     * @return {@code true} si el producto puede ser reembolsado según la
     *         política; {@code false} en caso contrario.
     */
    public boolean isProductReturnable(ProductId productId) {
        return returnableProductIds.isEmpty()
                || returnableProductIds.contains(productId);
    }
}
