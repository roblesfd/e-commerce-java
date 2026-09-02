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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse start(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                  @Valid @RequestBody StartCheckoutRequest request) {
        return mapper.toResponse(checkoutUseCases.start(
                SecurityUtils.requiredUserId(), idempotencyKey, mapper.toCommand(request)));
    }

    @GetMapping("/{id}")
    public CheckoutResponse get(@PathVariable UUID id) {
        return mapper.toResponse(checkoutUseCases.getById(SecurityUtils.requiredUserId(), CheckoutId.of(id)));
    }

    @PostMapping("/{id}/confirm")
    public CheckoutResponse confirm(@PathVariable UUID id) {
        return mapper.toResponse(checkoutUseCases.confirm(SecurityUtils.requiredUserId(), CheckoutId.of(id)));
    }

    @PostMapping("/{id}/cancel")
    public CheckoutResponse cancel(@PathVariable UUID id) {
        return mapper.toResponse(checkoutUseCases.cancel(SecurityUtils.requiredUserId(), CheckoutId.of(id)));
    }
}