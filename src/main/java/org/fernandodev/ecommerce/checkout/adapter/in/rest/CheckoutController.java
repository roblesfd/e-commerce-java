package org.fernandodev.ecommerce.checkout.adapter.in.rest;

import java.util.UUID;

import org.fernandodev.ecommerce.checkout.adapter.in.rest.dto.CheckoutResponse;
import org.fernandodev.ecommerce.checkout.adapter.in.rest.dto.StartCheckoutRequest;
import org.fernandodev.ecommerce.checkout.adapter.in.rest.mapper.CheckoutMapper;
import org.fernandodev.ecommerce.checkout.application.port.in.CheckoutUseCases;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutId;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST del contexto Checkout.
 */
@RestController
@RequestMapping("/api/v1/checkouts")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutUseCases checkoutUseCases;
    private final CheckoutMapper mapper;

    /**
     * Inicia un checkout (valida carrito, reserva inventario y aplica cupón).
     * Idempotente por el header {@code Idempotency-Key}.
     *
     * @param idempotencyKey clave de idempotencia.
     * @param request        dirección de envío.
     * @return el checkout en estado {@code PENDING}.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse start(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                  @Valid @RequestBody StartCheckoutRequest request) {
        return mapper.toResponse(checkoutUseCases.start(
                SecurityUtils.requiredUserId(), idempotencyKey, mapper.toCommand(request)));
    }

    /**
     * @param id identificador del checkout.
     * @return el checkout del usuario autenticado.
     */
    @GetMapping("/{id}")
    public CheckoutResponse get(@PathVariable UUID id) {
        return mapper.toResponse(checkoutUseCases.getById(SecurityUtils.requiredUserId(), CheckoutId.of(id)));
    }

    /**
     * Confirma el checkout (crea la orden e inicia el pago).
     *
     * @param id identificador del checkout.
     * @return el checkout en estado final.
     */
    @PostMapping("/{id}/confirm")
    public CheckoutResponse confirm(@PathVariable UUID id) {
        return mapper.toResponse(checkoutUseCases.confirm(SecurityUtils.requiredUserId(), CheckoutId.of(id)));
    }

    /**
     * Cancela el checkout en {@code PENDING}.
     *
     * @param id identificador del checkout.
     * @return el checkout cancelado.
     */
    @PostMapping("/{id}/cancel")
    public CheckoutResponse cancel(@PathVariable UUID id) {
        return mapper.toResponse(checkoutUseCases.cancel(SecurityUtils.requiredUserId(), CheckoutId.of(id)));
    }
}