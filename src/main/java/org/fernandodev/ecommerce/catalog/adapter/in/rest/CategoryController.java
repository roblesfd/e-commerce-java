package org.fernandodev.ecommerce.catalog.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.CategoryRequest;
import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.CategoryResponse;
import org.fernandodev.ecommerce.catalog.adapter.in.rest.mapper.CategoryMapper;
import org.fernandodev.ecommerce.catalog.application.port.in.CategoryUseCases;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
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
 * Controlador REST del contexto Product Catalog para categorías.
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryUseCases categoryUseCases;
    private final CategoryMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return mapper.toResponse(categoryUseCases.create(mapper.toCommand(request)));
    }

    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable UUID id) {
        return mapper.toResponse(categoryUseCases.getById(CategoryId.of(id)));
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryUseCases.list().stream().map(mapper::toResponse).toList();
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        return mapper.toResponse(categoryUseCases.update(CategoryId.of(id), mapper.toCommand(request)));
    }

    @PatchMapping("/{id}/activate")
    public CategoryResponse activate(@PathVariable UUID id) {
        return mapper.toResponse(categoryUseCases.activate(CategoryId.of(id)));
    }

    @PatchMapping("/{id}/deactivate")
    public CategoryResponse deactivate(@PathVariable UUID id) {
        return mapper.toResponse(categoryUseCases.deactivate(CategoryId.of(id)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        categoryUseCases.delete(CategoryId.of(id));
    }
}
