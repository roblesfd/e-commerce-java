package org.fernandodev.ecommerce.cart.adapter.in.rest;

import java.util.UUID;

import org.fernandodev.ecommerce.cart.adapter.in.rest.dto.AddItemRequest;
import org.fernandodev.ecommerce.cart.adapter.in.rest.dto.ApplyCouponRequest;
import org.fernandodev.ecommerce.cart.adapter.in.rest.dto.CartResponse;
import org.fernandodev.ecommerce.cart.adapter.in.rest.dto.TotalsResponse;
import org.fernandodev.ecommerce.cart.adapter.in.rest.dto.UpdateQuantityRequest;
import org.fernandodev.ecommerce.cart.adapter.in.rest.mapper.CartMapper;
import org.fernandodev.ecommerce.cart.application.port.in.CartUseCases;
import org.fernandodev.ecommerce.cart.domain.value_object.CartItemId;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST del contexto Shopping Cart.
 * <p>
 * El carrito se resuelve a partir del usuario IAM autenticado (recursos propios
 * por token).
 * </p>
 */
@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartUseCases cartUseCases;
    private final CartMapper mapper;

    /**
     * Crea el carrito del usuario autenticado si no existe.
     *
     * @return el carrito.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse create() {
        return mapper.toResponse(cartUseCases.createCart(SecurityUtils.requiredUserId()));
    }

    /**
     * Obtiene el carrito del usuario autenticado.
     *
     * @return el carrito del usuario autenticado.
     */
    @GetMapping("/me")
    public CartResponse me() {

        return mapper.toResponse(
                cartUseCases.getCart(SecurityUtils.requiredUserId())
        );
    }

    /**
     * Añade o incrementa un item en el carrito del usuario autenticado.
     *
     * @param request datos del item.
     * @return el carrito actualizado.
     */
    @PostMapping("/me/items")
    public CartResponse addItem(@Valid @RequestBody AddItemRequest request) {
        return mapper.toResponse(cartUseCases.addItem(
                SecurityUtils.requiredUserId(), mapper.toCommand(request))
        );
    }

    /**
     * Actualiza la cantidad de un item del carrito.
     *
     * @param itemId  identificador del item.
     * @param request nueva cantidad.
     * @return el carrito actualizado.
     */
    @PutMapping("/me/items/{itemId}")
    public CartResponse updateItemQuantity(@PathVariable UUID itemId,
                                           @Valid @RequestBody UpdateQuantityRequest request) {
        return mapper.toResponse(cartUseCases.updateItemQuantity(
                SecurityUtils.requiredUserId(),
                CartItemId.of(itemId),
                mapper.toCommand(request))
        );
    }

    /**
     * Elimina un item del carrito del usuario autenticado.
     *
     * @param itemId identificador del item.
     * @return el carrito actualizado.
     */
    @DeleteMapping("/me/items/{itemId}")
    public CartResponse removeItem(@PathVariable UUID itemId) {
        return mapper.toResponse(cartUseCases.removeItem(
                SecurityUtils.requiredUserId(), CartItemId.of(itemId))
        );
    }

    /**
     * Vacía el carrito del usuario autenticado.
     *
     * @return el carrito vacío.
     */
    @DeleteMapping("/me")
    public CartResponse clear() {

        return mapper.toResponse(
                cartUseCases.clearCart(SecurityUtils.requiredUserId())
        );
    }

    /**
     * Devuelve el desglose del carrito del usuario autenticado.
     *
     * @return subtotal, descuento y total del carrito del usuario autenticado.
     */
    @GetMapping("/me/totals")
    public TotalsResponse totals() {
        UUID ownerId = SecurityUtils.requiredUserId();
        return mapper.toTotalsResponse(
                cartUseCases.subtotal(ownerId),
                cartUseCases.discount(ownerId),
                cartUseCases.total(ownerId));
    }

    /**
     * Aplica un cupón al carrito del usuario autenticado.
     *
     * @param request código del cupón.
     * @return el carrito actualizado.
     */
    @PostMapping("/me/coupons")
    public CartResponse applyCoupon(@Valid @RequestBody ApplyCouponRequest request) {
        return mapper.toResponse(cartUseCases.applyCoupon(
                SecurityUtils.requiredUserId(), mapper.toCommand(request)));
    }

    /**
     * Elimina el cupón del carrito del usuario autenticado.
     *
     * @return el carrito actualizado.
     */
    @DeleteMapping("/me/coupons")
    public CartResponse removeCoupon() {
        return mapper.toResponse(cartUseCases.removeCoupon(SecurityUtils.requiredUserId()));
    }

    /**
     * Revalida el carrito del usuario autenticado (precios, stock y cupón).
     *
     * @return el carrito validado.
     */
    @PostMapping("/me/validate")
    public CartResponse validate() {
        return mapper.toResponse(cartUseCases.validateCart(SecurityUtils.requiredUserId()));
    }

    /**
     * Elimina el carrito del usuario autenticado (TTL vencido).
     */
    @PostMapping("/me/expire")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void expire() {
        cartUseCases.expireCart(SecurityUtils.requiredUserId());
    }
}
