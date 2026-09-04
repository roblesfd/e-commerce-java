package org.fernandodev.ecommerce.shipping.domain;

import java.util.Objects;

import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa un método de envío disponible en el sistema.
 *
 * <p>Un método de envío define la información necesaria para determinar
 * cómo será transportado un pedido, incluyendo su nombre, el carrier
 * responsable y el costo asociado.</p>
 *
 * <p>El agregado también controla su disponibilidad mediante el estado
 * {@code active}, permitiendo activar o desactivar el método de envío
 * sin necesidad de eliminarlo del sistema.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShippingMethod {

    /**
     * Identificador único del método de envío.
     */
    private ShippingMethodId id;

    /**
     * Nombre descriptivo del método de envío.
     *
     * <p>Por ejemplo: {@code Envío estándar}, {@code Envío express}.</p>
     */
    private String name;

    /**
     * Nombre del carrier o proveedor encargado de realizar el envío.
     *
     * <p>Por ejemplo: {@code DHL}, {@code FedEx} o {@code UPS}.</p>
     */
    private String carrier;

    /**
     * Costo asociado al método de envío.
     *
     * <p>El costo se representa mediante el Value Object {@code Money}
     * para garantizar la consistencia de la cantidad monetaria y su
     * moneda.</p>
     */
    private Money cost;

    /**
     * Indica si el método de envío se encuentra disponible para su uso.
     *
     * <p>Un método inactivo permanece registrado en el sistema, pero no
     * debe estar disponible para nuevas operaciones que requieran
     * seleccionar un método de envío.</p>
     */
    private boolean active;

    /**
     * Crea un nuevo método de envío activo.
     *
     * <p>Los métodos de envío creados mediante esta factoría comienzan
     * siempre en estado activo.</p>
     *
     * @param id identificador único del método de envío.
     * @param name nombre descriptivo del método de envío.
     * @param carrier carrier o proveedor encargado del envío.
     * @param cost costo asociado al método de envío.
     * @return una nueva instancia de {@code ShippingMethod} en estado activo.
     * @throws NullPointerException si {@code id} o {@code cost} son {@code null}.
     * @throws BusinessRuleViolationException si {@code name} o {@code carrier}
     *         son nulos, están vacíos o contienen únicamente espacios,
     *         o si el costo es negativo.
     */
    public static ShippingMethod create(
            ShippingMethodId id,
            String name,
            String carrier,
            Money cost
    ) {
        return crear(id, name, carrier, cost, true);
    }

    /**
     * Reconstruye un método de envío con el estado de actividad indicado.
     *
     * <p>Esta factoría permite crear una instancia especificando
     * explícitamente si el método se encuentra activo o inactivo.</p>
     *
     * <p>Los valores textuales {@code name} y {@code carrier} son
     * normalizados eliminando los espacios en blanco al inicio y al final.
     * Los campos obligatorios no pueden ser nulos ni contener únicamente
     * espacios.</p>
     *
     * @param id identificador único del método de envío.
     * @param name nombre descriptivo del método de envío.
     * @param carrier carrier o proveedor encargado del envío.
     * @param cost costo asociado al método de envío.
     * @param active indica si el método de envío debe quedar activo.
     * @return una nueva instancia de {@code ShippingMethod}.
     * @throws NullPointerException si {@code id} o {@code cost} son {@code null}.
     * @throws BusinessRuleViolationException si {@code name} o {@code carrier}
     *         son nulos, están vacíos o contienen únicamente espacios,
     *         o si el costo es negativo.
     */
    public static ShippingMethod crear(
            ShippingMethodId id,
            String name,
            String carrier,
            Money cost,
            boolean active
    ) {
        ShippingMethod method = new ShippingMethod();

        method.id = Objects.requireNonNull(id, "id no puede ser null");
        method.name = requireText(name, "name");
        method.carrier = requireText(carrier, "carrier");
        method.cost = Objects.requireNonNull(cost, "cost no puede ser null");

        if (cost.isNegativeOrZero() && !cost.isZero()) {
            throw new BusinessRuleViolationException(
                    "El costo del envío no puede ser negativo"
            );
        }

        method.active = active;

        return method;
    }

    /**
     * Activa el método de envío.
     *
     * <p>Una vez activado, el método puede volver a estar disponible
     * para las operaciones que permitan seleccionar métodos de envío.</p>
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Desactiva el método de envío.
     *
     * <p>La desactivación no elimina el método del sistema; únicamente
     * impide que sea considerado disponible para nuevas operaciones.</p>
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Valida y normaliza un valor textual obligatorio.
     *
     * <p>El valor debe existir y contener al menos un carácter distinto
     * de un espacio en blanco. Una vez validado, se eliminan los espacios
     * al inicio y al final.</p>
     *
     * @param value valor textual que debe validarse.
     * @param name nombre del campo utilizado para construir el mensaje
     *             de la regla de negocio.
     * @return el valor validado y normalizado.
     * @throws BusinessRuleViolationException si {@code value} es
     *         {@code null}, está vacío o contiene únicamente espacios.
     */
    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException(name + " es obligatorio");
        }

        return value.trim();
    }
}
