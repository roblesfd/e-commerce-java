package org.fernandodev.ecommerce.promotion.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.PromotionRequest;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.PromotionResponse;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.UpdatePromotionRequest;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.mapper.PromotionMapper;
import org.fernandodev.ecommerce.promotion.application.port.in.PromotionUseCases;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
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
 * Controlador REST del contexto Promotions & Coupons para promociones.
 */
@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionUseCases promotionUseCases;
    private final PromotionMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionResponse create(@Valid @RequestBody PromotionRequest request) {
        return mapper.toResponse(promotionUseCases.create(mapper.toCreateCommand(request)));
    }

    @GetMapping("/{id}")
    public PromotionResponse get(@PathVariable UUID id) {
        return mapper.toResponse(promotionUseCases.getById(PromotionId.of(id)));
    }

    @GetMapping
    public List<PromotionResponse> list() {
        return promotionUseCases.list().stream().map(mapper::toResponse).toList();
    }

    @PutMapping("/{id}")
    public PromotionResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePromotionRequest request) {
        return mapper.toResponse(promotionUseCases.update(PromotionId.of(id), mapper.toUpdateCommand(request)));
    }

    @PatchMapping("/{id}/activate")
    public PromotionResponse activate(@PathVariable UUID id) {
        return mapper.toResponse(promotionUseCases.activate(PromotionId.of(id)));
    }

    @PatchMapping("/{id}/deactivate")
    public PromotionResponse deactivate(@PathVariable UUID id) {
        return mapper.toResponse(promotionUseCases.deactivate(PromotionId.of(id)));
    }

    @PostMapping("/{id}/products")
    public PromotionResponse addProduct(@PathVariable UUID id, @RequestBody UUID productId) {
        return mapper.toResponse(promotionUseCases.addProduct(PromotionId.of(id), ProductId.of(productId)));
    }

    @DeleteMapping("/{id}/products/{productId}")
    public PromotionResponse removeProduct(@PathVariable UUID id, @PathVariable UUID productId) {
        return mapper.toResponse(promotionUseCases.removeProduct(PromotionId.of(id), ProductId.of(productId)));
    }

    @PostMapping("/{id}/categories")
    public PromotionResponse addCategory(@PathVariable UUID id, @RequestBody UUID categoryId) {
        return mapper.toResponse(promotionUseCases.addCategory(PromotionId.of(id), CategoryId.of(categoryId)));
    }

    @DeleteMapping("/{id}/categories/{categoryId}")
    public PromotionResponse removeCategory(@PathVariable UUID id, @PathVariable UUID categoryId) {
        return mapper.toResponse(promotionUseCases.removeCategory(PromotionId.of(id), CategoryId.of(categoryId)));
    }

    @PostMapping("/{id}/usage")
    public PromotionResponse recordUsage(@PathVariable UUID id) {
        return mapper.toResponse(promotionUseCases.recordUsage(PromotionId.of(id), SecurityUtils.requiredUserId()));
    }
}