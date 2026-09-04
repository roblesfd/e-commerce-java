package org.fernandodev.ecommerce.promotion.domain;

import java.time.Instant;
import java.util.Objects;

import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa un cupón dentro del contexto de promociones.
 *
 * <p>Un cupón es un código canjeable asociado a una promoción y cuya utilización
 * está condicionada por su estado de activación y por un periodo de vigencia.</p>
 *
 * <p>El agregado garantiza las siguientes reglas de negocio:</p>
 * <ul>
 *     <li>El identificador, código y promoción asociada son obligatorios.</li>
 *     <li>El código no puede estar vacío ni superar los 64 caracteres.</li>
 *     <li>El código se normaliza eliminando espacios y convirtiéndolo a
 *         mayúsculas.</li>
 *     <li>Las fechas de vigencia son obligatorias.</li>
 *     <li>La fecha de inicio debe ser anterior a la fecha de finalización.</li>
 * </ul>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Coupon {

    /**
     * Identificador único del cupón.
     */
    private CouponId id;

    /**
     * Código que identifica al cupón y que puede ser utilizado para canjear
     * la promoción asociada.
     */
    private String code;

    /**
     * Identificador de la promoción a la que está asociado el cupón.
     */
    private PromotionId promotionId;

    /**
     * Indica si el cupón se encuentra habilitado para su utilización.
     */
    private boolean active;

    /**
     * Fecha y hora a partir de la cual el cupón comienza a ser válido.
     */
    private Instant validFrom;

    /**
     * Fecha y hora hasta la cual el cupón permanece vigente.
     *
     * <p>El límite superior es exclusivo, por lo que el cupón deja de ser
     * válido exactamente en este instante.</p>
     */
    private Instant validTo;

    /**
     * Crea un nuevo cupón activo.
     *
     * <p>El cupón se crea inicialmente habilitado y con el periodo de
     * vigencia especificado.</p>
     *
     * @param id identificador único del cupón.
     * @param code código que podrá utilizarse para canjear el cupón.
     * @param promotionId identificador de la promoción asociada.
     * @param validFrom instante a partir del cual el cupón es válido.
     * @param validTo instante a partir del cual el cupón deja de ser válido.
     * @return una nueva instancia de {@code Coupon}.
     * @throws NullPointerException si {@code id}, {@code promotionId},
     *         {@code validFrom} o {@code validTo} son {@code null}.
     * @throws BusinessRuleViolationException si el código es inválido o
     *         {@code validFrom} no es anterior a {@code validTo}.
     */
    public static Coupon create(CouponId id, String code, PromotionId promotionId,
                                Instant validFrom, Instant validTo) {
        return crear(id, code, promotionId, true, validFrom, validTo);
    }

    /**
     * Crea un nuevo cupón con el estado de activación indicado.
     *
     * <p>Este método centraliza la creación del agregado y garantiza que todas
     * las invariantes necesarias sean validadas antes de devolver la instancia.</p>
     *
     * @param id identificador único del cupón.
     * @param code código que podrá utilizarse para canjear el cupón.
     * @param promotionId identificador de la promoción asociada.
     * @param active indica si el cupón debe crearse habilitado.
     * @param validFrom instante a partir del cual el cupón es válido.
     * @param validTo instante a partir del cual el cupón deja de ser válido.
     * @return una nueva instancia de {@code Coupon}.
     * @throws NullPointerException si {@code id}, {@code promotionId},
     *         {@code validFrom} o {@code validTo} son {@code null}.
     * @throws BusinessRuleViolationException si el código es inválido o
     *         {@code validFrom} no es anterior a {@code validTo}.
     */
    public static Coupon crear(CouponId id, String code, PromotionId promotionId,
                               boolean active, Instant validFrom, Instant validTo) {
        Coupon coupon = new Coupon();
        coupon.id = Objects.requireNonNull(id, "id no puede ser null");
        coupon.code = requireCode(code);
        coupon.promotionId = Objects.requireNonNull(
                promotionId,
                "promotionId no puede ser null"
        );
        coupon.active = active;

        Objects.requireNonNull(validFrom, "validFrom no puede ser null");
        Objects.requireNonNull(validTo, "validTo no puede ser null");

        if (!validFrom.isBefore(validTo)) {
            throw new BusinessRuleViolationException(
                    "validFrom debe ser anterior a validTo"
            );
        }

        coupon.validFrom = validFrom;
        coupon.validTo = validTo;

        return coupon;
    }

    /**
     * Determina si el cupón puede utilizarse en un instante determinado.
     *
     * <p>Un cupón se considera válido cuando se encuentra activo y el instante
     * consultado pertenece a su periodo de vigencia.</p>
     *
     * <p>El inicio de vigencia es inclusivo y el final de vigencia es
     * exclusivo:</p>
     *
     * <pre>
     * validFrom &lt;= at &lt; validTo
     * </pre>
     *
     * @param at instante en el que se desea comprobar la validez del cupón.
     * @return {@code true} si el cupón está activo y vigente en el instante
     *         indicado; {@code false} en caso contrario.
     */
    public boolean isActiveAt(Instant at) {
        return active && !at.isBefore(validFrom) && at.isBefore(validTo);
    }

    /**
     * Activa el cupón.
     *
     * <p>Una vez activado, el cupón podrá considerarse utilizable siempre que
     * el instante de consulta se encuentre dentro de su periodo de vigencia.</p>
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Desactiva el cupón.
     *
     * <p>Un cupón desactivado no puede considerarse válido, incluso aunque
     * el instante de consulta se encuentre dentro de su periodo de vigencia.</p>
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Actualiza el periodo de vigencia del cupón.
     *
     * <p>La modificación mantiene la invariante de que la fecha de inicio
     * debe ser estrictamente anterior a la fecha de finalización.</p>
     *
     * @param validFrom nuevo instante a partir del cual el cupón será válido.
     * @param validTo nuevo instante hasta el cual el cupón será válido.
     * @throws NullPointerException si {@code validFrom} o {@code validTo}
     *         son {@code null}.
     * @throws BusinessRuleViolationException si {@code validFrom} no es
     *         anterior a {@code validTo}.
     */
    public void setValidity(Instant validFrom, Instant validTo) {
        Objects.requireNonNull(validFrom, "validFrom no puede ser null");
        Objects.requireNonNull(validTo, "validTo no puede ser null");

        if (!validFrom.isBefore(validTo)) {
            throw new BusinessRuleViolationException(
                    "validFrom debe ser anterior a validTo"
            );
        }

        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    /**
     * Valida y normaliza el código del cupón.
     *
     * <p>El código debe ser obligatorio y no puede superar los 64 caracteres.
     * Una vez validado, se eliminan los espacios al inicio y al final y se
     * convierte a mayúsculas utilizando {@link java.util.Locale#ROOT} para
     * garantizar una normalización independiente de la configuración
     * regional del sistema.</p>
     *
     * @param code código que se desea validar y normalizar.
     * @return el código validado y normalizado.
     * @throws BusinessRuleViolationException si el código es {@code null},
     *         está vacío, contiene únicamente espacios o supera los 64
     *         caracteres.
     */
    private static String requireCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessRuleViolationException(
                    "El código del cupón es obligatorio"
            );
        }

        if (code.length() > 64) {
            throw new BusinessRuleViolationException(
                    "El código del cupón es demasiado largo"
            );
        }

        return code.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
