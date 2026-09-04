package org.fernandodev.ecommerce.wishlist.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistId;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Agregado raíz que representa una wishlist perteneciente a un cliente.
 *
 * <p>Una wishlist permite al cliente mantener una colección de productos
 * o variantes del catálogo que desea conservar para una consulta o compra
 * posterior.</p>
 *
 * <p>El agregado soporta múltiples wishlists para un mismo cliente y
 * mantiene únicamente referencias a los productos y variantes del
 * catálogo, evitando duplicar información perteneciente a otros
 * contextos.</p>
 *
 * <p>Una wishlist no permite registrar dos veces el mismo elemento,
 * considerando como identidad del elemento la combinación de
 * {@code productId} y {@code variantId}.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Wishlist {

    /**
     * Identificador único de la wishlist.
     */
    private WishlistId id;

    /**
     * Identificador del cliente propietario de la wishlist.
     */
    private UUID customerId;

    /**
     * Nombre descriptivo de la wishlist.
     *
     * <p>El nombre es obligatorio y no puede estar vacío ni contener
     * únicamente espacios en blanco.</p>
     */
    private String name;

    /**
     * Elementos contenidos en la wishlist.
     *
     * <p>El acceso mediante Lombok está restringido para evitar exponer
     * directamente la colección mutable interna del agregado.</p>
     */
    @Getter(AccessLevel.NONE)
    private List<WishlistItem> items;

    /**
     * Fecha y hora en la que se creó la wishlist.
     */
    private Instant createdAt;

    /**
     * Fecha y hora de la última modificación realizada sobre la wishlist.
     */
    private Instant updatedAt;

    /**
     * Crea una nueva wishlist vacía.
     *
     * <p>La wishlist se crea sin elementos y con las fechas de creación
     * y actualización establecidas al instante proporcionado.</p>
     *
     * @param id identificador único de la wishlist.
     * @param customerId identificador del cliente propietario.
     * @param name nombre de la wishlist.
     * @param at instante en el que se crea la wishlist.
     * @return nueva wishlist vacía.
     * @throws NullPointerException si {@code id}, {@code customerId},
     *         {@code name} o {@code at} son {@code null}.
     * @throws BusinessRuleViolationException si {@code name} está vacío
     *         o contiene únicamente espacios en blanco.
     */
    public static Wishlist create(
            WishlistId id,
            UUID customerId,
            String name,
            Instant at
    ) {
        Wishlist wishlist = new Wishlist();

        wishlist.id = Objects.requireNonNull(id, "id no puede ser null");
        wishlist.customerId = Objects.requireNonNull(
                customerId,
                "customerId no puede ser null"
        );
        wishlist.name = validateName(name);
        wishlist.items = new ArrayList<>();
        wishlist.createdAt = Objects.requireNonNull(
                at,
                "at no puede ser null"
        );
        wishlist.updatedAt = at;

        return wishlist;
    }

    /**
     * Reconstruye una wishlist a partir de su estado existente.
     *
     * <p>Esta factoría está destinada principalmente a la reconstrucción
     * del agregado desde la persistencia.</p>
     *
     * <p>La colección de elementos recibida se copia defensivamente para
     * evitar que modificaciones posteriores sobre la colección original
     * alteren directamente el estado interno del agregado.</p>
     *
     * @param id identificador único de la wishlist.
     * @param customerId identificador del cliente propietario.
     * @param name nombre de la wishlist.
     * @param items elementos actualmente contenidos en la wishlist.
     * @param createdAt fecha y hora de creación de la wishlist.
     * @param updatedAt fecha y hora de la última actualización.
     * @return wishlist reconstruida con el estado proporcionado.
     * @throws NullPointerException si {@code id}, {@code customerId},
     *         {@code name}, {@code createdAt} o {@code updatedAt} son
     *         {@code null}.
     * @throws BusinessRuleViolationException si {@code name} está vacío
     *         o contiene únicamente espacios en blanco.
     */
    public static Wishlist crear(
            WishlistId id,
            UUID customerId,
            String name,
            List<WishlistItem> items,
            Instant createdAt,
            Instant updatedAt
    ) {
        Wishlist wishlist = new Wishlist();

        wishlist.id = Objects.requireNonNull(
                id,
                "id no puede ser null"
        );
        wishlist.customerId = Objects.requireNonNull(
                customerId,
                "customerId no puede ser null"
        );
        wishlist.name = validateName(name);
        wishlist.items = new ArrayList<>(
                items == null ? List.of() : items
        );
        wishlist.createdAt = Objects.requireNonNull(
                createdAt,
                "createdAt no puede ser null"
        );
        wishlist.updatedAt = Objects.requireNonNull(
                updatedAt,
                "updatedAt no puede ser null"
        );

        return wishlist;
    }

    /**
     * Obtiene los elementos contenidos en la wishlist.
     *
     * <p>La colección retornada es de solo lectura para impedir que
     * código externo modifique directamente el estado interno del
     * agregado. Las modificaciones deben realizarse mediante los
     * métodos de comportamiento proporcionados por {@code Wishlist}.</p>
     *
     * @return vista no modificable de los elementos de la wishlist.
     */
    public List<WishlistItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Cambia el nombre de la wishlist.
     *
     * <p>El nuevo nombre se valida y normaliza antes de almacenarse.
     * La fecha de actualización se establece en el instante indicado.</p>
     *
     * @param name nuevo nombre de la wishlist.
     * @param at instante en el que se realiza el cambio.
     * @throws NullPointerException si {@code name} es {@code null}.
     * @throws BusinessRuleViolationException si {@code name} está vacío
     *         o contiene únicamente espacios en blanco.
     */
    public void rename(String name, Instant at) {
        this.name = validateName(name);
        this.updatedAt = at;
    }

    /**
     * Agrega un producto o variante a la wishlist.
     *
     * <p>Un mismo producto no puede registrarse dos veces con la misma
     * variante. La unicidad se determina mediante la combinación de
     * {@code productId} y {@code variantId}.</p>
     *
     * <p>Cuando {@code variantId} es {@code null}, la comparación se
     * realiza considerando que se trata del producto sin una variante
     * específica.</p>
     *
     * @param productId identificador del producto que se desea agregar.
     * @param variantId identificador opcional de la variante del producto.
     * @param at instante en el que se agrega el elemento.
     * @throws BusinessRuleViolationException si el elemento ya existe
     *         en la wishlist.
     */
    public void addItem(
            ProductId productId,
            ProductVariantId variantId,
            Instant at
    ) {
        boolean exists = items.stream().anyMatch(item ->
                item.productId().equals(productId)
                        && Objects.equals(item.variantId(), variantId)
        );

        if (exists) {
            throw new BusinessRuleViolationException(
                    "El item ya está en la wishlist"
            );
        }

        items.add(new WishlistItem(productId, variantId, at));
        this.updatedAt = at;
    }

    /**
     * Elimina un producto o variante de la wishlist.
     *
     * <p>La eliminación utiliza la misma combinación de
     * {@code productId} y {@code variantId} utilizada para determinar
     * la unicidad de los elementos.</p>
     *
     * @param productId identificador del producto que se desea eliminar.
     * @param variantId identificador opcional de la variante del producto.
     * @param at instante en el que se elimina el elemento.
     * @throws BusinessRuleViolationException si el elemento no existe
     *         en la wishlist.
     */
    public void removeItem(
            ProductId productId,
            ProductVariantId variantId,
            Instant at
    ) {
        boolean removed = items.removeIf(item ->
                item.productId().equals(productId)
                        && Objects.equals(item.variantId(), variantId)
        );

        if (!removed) {
            throw new BusinessRuleViolationException(
                    "El item no está en la wishlist"
            );
        }

        this.updatedAt = at;
    }

    /**
     * Elimina todos los elementos de la wishlist.
     *
     * <p>La wishlist permanece existente y conserva su identidad,
     * nombre y propietario; únicamente se eliminan los elementos
     * almacenados en ella.</p>
     *
     * @param at instante en el que se vacía la wishlist.
     */
    public void clear(Instant at) {
        items.clear();
        this.updatedAt = at;
    }

    /**
     * Valida y normaliza el nombre de la wishlist.
     *
     * <p>El nombre debe existir y contener al menos un carácter distinto
     * de un espacio en blanco. Una vez validado, se eliminan los espacios
     * existentes al inicio y al final del valor.</p>
     *
     * @param name nombre que debe validarse.
     * @return nombre validado y normalizado.
     * @throws NullPointerException si {@code name} es {@code null}.
     * @throws BusinessRuleViolationException si {@code name} está vacío
     *         o contiene únicamente espacios en blanco.
     */
    private static String validateName(String name) {
        Objects.requireNonNull(name, "name no puede ser null");

        if (name.isBlank()) {
            throw new BusinessRuleViolationException(
                    "El nombre de la wishlist no puede estar vacío"
            );
        }

        return name.trim();
    }
}
