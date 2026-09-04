package org.fernandodev.ecommerce.shipping.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.ShippingMethodRequest;
import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.ShippingMethodResponse;
import org.fernandodev.ecommerce.shipping.adapter.in.rest.mapper.ShippingMethodMapper;
import org.fernandodev.ecommerce.shipping.application.port.in.ShippingMethodUseCases;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de métodos de envío.
 */
@RestController
@RequestMapping("/api/v1/shipping-methods")
@RequiredArgsConstructor
public class ShippingMethodController {

    private final ShippingMethodUseCases shippingMethodUseCases;
    private final ShippingMethodMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShippingMethodResponse create(@Valid @RequestBody ShippingMethodRequest request) {
        return mapper.toResponse(shippingMethodUseCases.create(mapper.toCommand(request)));
    }

    @GetMapping
    /**
     * List.
     */
    public List<ShippingMethodResponse> list() {
        return shippingMethodUseCases.list().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    /**
     * Get.
     * @param id id.
     */
    public ShippingMethodResponse get(@PathVariable UUID id) {
        return mapper.toResponse(shippingMethodUseCases.getById(ShippingMethodId.of(id)));
    }

    @PatchMapping("/{id}/activate")
    /**
     * Activate.
     * @param id id.
     */
    public ShippingMethodResponse activate(@PathVariable UUID id) {
        return mapper.toResponse(shippingMethodUseCases.activate(ShippingMethodId.of(id)));
    }

    @PatchMapping("/{id}/deactivate")
    /**
     * Deactivate.
     * @param id id.
     */
    public ShippingMethodResponse deactivate(@PathVariable UUID id) {
        return mapper.toResponse(shippingMethodUseCases.deactivate(ShippingMethodId.of(id)));
    }
}