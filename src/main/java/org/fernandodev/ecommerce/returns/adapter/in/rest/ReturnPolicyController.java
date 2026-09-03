package org.fernandodev.ecommerce.returns.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.CreateReturnPolicyRequest;
import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.ReturnPolicyResponse;
import org.fernandodev.ecommerce.returns.adapter.in.rest.mapper.ReturnPolicyMapper;
import org.fernandodev.ecommerce.returns.application.port.in.ReturnPolicyUseCases;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnPolicyId;
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
 * Controlador REST de políticas de devoluciones.
 */
@RestController
@RequestMapping("/api/v1/return-policies")
@RequiredArgsConstructor
public class ReturnPolicyController {

    private final ReturnPolicyUseCases returnPolicyUseCases;
    private final ReturnPolicyMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnPolicyResponse create(@Valid @RequestBody CreateReturnPolicyRequest request) {
        return mapper.toResponse(returnPolicyUseCases.create(mapper.toCommand(request)));
    }

    @GetMapping
    public List<ReturnPolicyResponse> list() {
        return returnPolicyUseCases.list().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ReturnPolicyResponse get(@PathVariable UUID id) {
        return mapper.toResponse(returnPolicyUseCases.getById(ReturnPolicyId.of(id)));
    }

    @PatchMapping("/{id}/activate")
    public ReturnPolicyResponse activate(@PathVariable UUID id) {
        return mapper.toResponse(returnPolicyUseCases.activate(ReturnPolicyId.of(id)));
    }

    @PatchMapping("/{id}/deactivate")
    public ReturnPolicyResponse deactivate(@PathVariable UUID id) {
        return mapper.toResponse(returnPolicyUseCases.deactivate(ReturnPolicyId.of(id)));
    }
}