package org.fernandodev.ecommerce.catalog.adapter.in.rest;

import java.util.UUID;

import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.PageResponse;
import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.ProductRequest;
import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.ProductResponse;
import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.ProductSummaryResponse;
import org.fernandodev.ecommerce.catalog.adapter.in.rest.mapper.ProductMapper;
import org.fernandodev.ecommerce.catalog.application.port.in.ProductUseCases;
import org.fernandodev.ecommerce.catalog.application.dto.PageQuery;
import org.fernandodev.ecommerce.catalog.application.dto.ProductSearch;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST del contexto Product Catalog para productos.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCases productUseCases;
    private final ProductMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return mapper.toResponse(productUseCases.create(mapper.toCommand(request)));
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable UUID id) {
        return mapper.toResponse(productUseCases.getById(ProductId.of(id)));
    }

    @GetMapping
    public PageResponse<ProductSummaryResponse> list(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        ProductSearch search = new ProductSearch(
                active,
                categoryId == null ? null : CategoryId.of(categoryId),
                brandId == null ? null : BrandId.of(brandId),
                q);
        PageQuery query = new PageQuery(page, size, sortBy, sortDirection);
        return mapper.toPageResponse(productUseCases.list(search, query));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return mapper.toResponse(productUseCases.update(ProductId.of(id), mapper.toCommand(request)));
    }

    @PatchMapping("/{id}/activate")
    public ProductResponse activate(@PathVariable UUID id) {
        return mapper.toResponse(productUseCases.activate(ProductId.of(id)));
    }

    @PatchMapping("/{id}/deactivate")
    public ProductResponse deactivate(@PathVariable UUID id) {
        return mapper.toResponse(productUseCases.deactivate(ProductId.of(id)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        productUseCases.delete(ProductId.of(id));
    }
}
