package org.fernandodev.ecommerce.reviews.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.reviews.adapter.in.rest.dto.AddReplyRequest;
import org.fernandodev.ecommerce.reviews.adapter.in.rest.dto.CreateReviewRequest;
import org.fernandodev.ecommerce.reviews.adapter.in.rest.dto.RatingSummaryResponse;
import org.fernandodev.ecommerce.reviews.adapter.in.rest.dto.ReviewResponse;
import org.fernandodev.ecommerce.reviews.adapter.in.rest.dto.UpdateReviewRequest;
import org.fernandodev.ecommerce.reviews.adapter.in.rest.mapper.RatingSummaryMapper;
import org.fernandodev.ecommerce.reviews.adapter.in.rest.mapper.ReviewMapper;
import org.fernandodev.ecommerce.reviews.application.port.in.ReviewUseCases;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
 * Controlador REST de reviews y ratings de productos.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewUseCases reviewUseCases;
    private final ReviewMapper mapper;
    private final RatingSummaryMapper ratingSummaryMapper;

    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@Valid @RequestBody CreateReviewRequest request) {
        return mapper.toResponse(reviewUseCases.create(
                SecurityUtils.requiredUserId(), ProductId.of(request.productId()), mapper.toCreateCommand(request)));
    }

    @PutMapping("/reviews/{id}")
    public ReviewResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateReviewRequest request) {
        return mapper.toResponse(reviewUseCases.update(ReviewId.of(id), mapper.toUpdateCommand(request)));
    }

    @DeleteMapping("/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        reviewUseCases.delete(ReviewId.of(id));
    }

    @GetMapping("/reviews/{id}")
    public ReviewResponse get(@PathVariable UUID id) {
        return mapper.toResponse(reviewUseCases.getById(ReviewId.of(id)));
    }

    @GetMapping("/reviews/me")
    public List<ReviewResponse> mine() {
        return reviewUseCases.listByCustomer(SecurityUtils.requiredUserId()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/reviews")
    public List<ReviewResponse> moderation(@RequestParam(required = false) ReviewStatus status) {
        return reviewUseCases.listForModeration().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PostMapping("/reviews/{id}/approve")
    public ReviewResponse approve(@PathVariable UUID id) {
        return mapper.toResponse(reviewUseCases.approve(ReviewId.of(id)));
    }

    @PostMapping("/reviews/{id}/reject")
    public ReviewResponse reject(@PathVariable UUID id) {
        return mapper.toResponse(reviewUseCases.reject(ReviewId.of(id)));
    }

    @PostMapping("/reviews/{id}/hide")
    public ReviewResponse hide(@PathVariable UUID id) {
        return mapper.toResponse(reviewUseCases.hide(ReviewId.of(id)));
    }

    @PostMapping("/reviews/{id}/restore")
    public ReviewResponse restore(@PathVariable UUID id) {
        return mapper.toResponse(reviewUseCases.restore(ReviewId.of(id)));
    }

    @PostMapping("/reviews/{id}/replies")
    public ReviewResponse addReply(@PathVariable UUID id, @Valid @RequestBody AddReplyRequest request) {
        return mapper.toResponse(reviewUseCases.addReply(ReviewId.of(id), SecurityUtils.requiredUserId(),
                request.comment()));
    }

    @GetMapping("/products/{productId}/reviews")
    public List<ReviewResponse> listByProduct(@PathVariable UUID productId) {
        return reviewUseCases.listByProduct(ProductId.of(productId)).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/products/{productId}/rating")
    public RatingSummaryResponse rating(@PathVariable UUID productId) {
        return ratingSummaryMapper.toResponse(reviewUseCases.ratingSummary(ProductId.of(productId)));
    }
}