package org.fernandodev.ecommerce.catalog.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.BrandRequest;
import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.BrandResponse;
import org.fernandodev.ecommerce.catalog.adapter.in.rest.mapper.BrandMapper;
import org.fernandodev.ecommerce.catalog.application.port.in.BrandUseCases;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
 * Controlador REST del contexto Product Catalog para marcas.
 */
@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandUseCases brandUseCases;
    private final BrandMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BrandResponse create(@Valid @RequestBody BrandRequest request) {
        return mapper.toResponse(brandUseCases.create(mapper.toCommand(request)));
    }

    @GetMapping("/{id}")
    public BrandResponse get(@PathVariable UUID id) {
        return mapper.toResponse(brandUseCases.getById(BrandId.of(id)));
    }

    @GetMapping
    public List<BrandResponse> list() {
        return brandUseCases.list().stream().map(mapper::toResponse).toList();
    }

    @PutMapping("/{id}")
    public BrandResponse update(@PathVariable UUID id, @Valid @RequestBody BrandRequest request) {
        return mapper.toResponse(brandUseCases.update(BrandId.of(id), mapper.toCommand(request)));
    }

    @PatchMapping("/{id}/activate")
    public BrandResponse activate(@PathVariable UUID id) {
        return mapper.toResponse(brandUseCases.activate(BrandId.of(id)));
    }

    @PatchMapping("/{id}/deactivate")
    public BrandResponse deactivate(@PathVariable UUID id) {
        return mapper.toResponse(brandUseCases.deactivate(BrandId.of(id)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        brandUseCases.delete(BrandId.of(id));
    }
}
