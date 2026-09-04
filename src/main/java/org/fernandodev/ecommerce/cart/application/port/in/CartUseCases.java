package org.fernandodev.ecommerce.cart.application.port.in;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.cart.application.dto.AddItemCommand;
import org.fernandodev.ecommerce.cart.application.dto.ApplyCouponCommand;
import org.fernandodev.ecommerce.cart.application.dto.UpdateQuantityCommand;
import org.fernandodev.ecommerce.cart.domain.Cart;
import org.fernandodev.ecommerce.cart.domain.value_object.CartItemId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Casos de uso del contexto Shopping Cart (puerto de entrada).
 */
public interface CartUseCases {

    /**
     * Crea el carrito del propietario si no existe.
     *
     * @param ownerId identificador del propietario (usuario IAM).
     * @return el carrito creado.
     */
    Cart createCart(UUID ownerId);

    /**
     * @param ownerId identificador del propietario (usuario IAM).
     * @return el carrito del propietario.
     * @throws org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException
     *         si no existe.
     */
    Cart getCart(UUID ownerId);

    /**
     * Añade o incrementa un item, validando disponibilidad de stock y capturando
     * el precio vigente.
     *
     * @param ownerId identificador del propietario (usuario IAM).
     * @param command datos del item.
     * @return el carrito actualizado.
     */
    Cart addItem(UUID ownerId, AddItemCommand command);

    /**
     * Actualiza la cantidad de un item.
     *
     * @param ownerId identificador del propietario (usuario IAM).
     * @param itemId  identificador del item.
     * @param command nueva cantidad.
     * @return el carrito actualizado.
     */
    Cart updateItemQuantity(UUID ownerId, CartItemId itemId, UpdateQuantityCommand command);

    /**
     * Elimina un item del carrito.
     *
     * @param ownerId identificador del propietario (usuario IAM).
     * @param itemId  identificador del item.
     * @return el carrito actualizado.
     */
    Cart removeItem(UUID ownerId, CartItemId itemId);

    /**
     * Vacía el carrito del propietario.
     *
     * @param ownerId identificador del propietario (usuario IAM).
     * @return el carrito vacío.
     */
    Cart clearCart(UUID ownerId);

    /**
     * Aplica un cupón al carrito.
     *
     * @param ownerId identificador del propietario (usuario IAM).
     * @param command datos del cupón.
     * @return el carrito actualizado.
     */
    Cart applyCoupon(UUID ownerId, ApplyCouponCommand command);

    /**
     * Elimina el cupón aplicado al carrito.
     *
     * @param ownerId identificador del propietario (usuario IAM).
     * @return el carrito actualizado.
     */
    Cart removeCoupon(UUID ownerId);

    /**
     * @param ownerId identificador del propietario (usuario IAM).
     * @return el subtotal del carrito, o {@code Optional.empty()} si está vacío.
     */
    Optional<Money> subtotal(UUID ownerId);

    /**
     * @param ownerId identificador del propietario (usuario IAM).
     * @return el descuento del carrito, o {@code Optional.empty()} si está vacío.
     */
    Optional<Money> discount(UUID ownerId);

    /**
     * @param ownerId identificador del propietario (usuario IAM).
     * @return el total del carrito, o {@code Optional.empty()} si está vacío.
     */
    Optional<Money> total(UUID ownerId);

    /**
     * Revalida el carrito: precios vigentes, disponibilidad y cupón.
     *
     * @param ownerId identificador del propietario (usuario IAM).
     * @return el carrito validado.
     */
    Cart validateCart(UUID ownerId);

    /**
     * Elimina el carrito (TTL vencido).
     *
     * @param ownerId identificador del propietario (usuario IAM).
     */
    void expireCart(UUID ownerId);
}