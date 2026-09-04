package org.fernandodev.ecommerce.promotion.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.ApplyCouponRequest;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.CouponApplicationResponse;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.CouponRequest;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.CouponResponse;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.CouponValidationResponse;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.UpdateCouponRequest;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.mapper.CouponMapper;
import org.fernandodev.ecommerce.promotion.application.port.in.CouponUseCases;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
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
 * Controlador REST del contexto Promotions & Coupons para cupones.
 */
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponUseCases couponUseCases;
    private final CouponMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse create(@Valid @RequestBody CouponRequest request) {
        return mapper.toResponse(couponUseCases.create(mapper.toCreateCommand(request)));
    }

    @GetMapping("/{id}")
    /**
     * Get.
     * @param id id.
     */
    public CouponResponse get(@PathVariable UUID id) {
        return mapper.toResponse(couponUseCases.getById(CouponId.of(id)));
    }

    @GetMapping
    /**
     * List.
     */
    public List<CouponResponse> list() {
        return couponUseCases.list().stream().map(mapper::toResponse).toList();
    }

    @PutMapping("/{id}")
    /**
     * Update.
     * @param id id.
     * @param request request.
     */
    public CouponResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateCouponRequest request) {
        return mapper.toResponse(couponUseCases.update(CouponId.of(id), mapper.toUpdateCommand(request)));
    }

    @PatchMapping("/{id}/activate")
    /**
     * Activate.
     * @param id id.
     */
    public CouponResponse activate(@PathVariable UUID id) {
        return mapper.toResponse(couponUseCases.activate(CouponId.of(id)));
    }

    @PatchMapping("/{id}/deactivate")
    /**
     * Deactivate.
     * @param id id.
     */
    public CouponResponse deactivate(@PathVariable UUID id) {
        return mapper.toResponse(couponUseCases.deactivate(CouponId.of(id)));
    }

    @PostMapping("/validate")
    /**
     * Validate.
     * @param request request.
     */
    public CouponValidationResponse validate(@Valid @RequestBody ApplyCouponRequest request) {
        return mapper.toValidationResponse(couponUseCases.validateCoupon(
                request.code(), SecurityUtils.requiredUserId(), mapper.toApplyCommand(request)));
    }

    @PostMapping("/apply")
    /**
     * Apply.
     * @param request request.
     */
    public CouponApplicationResponse apply(@Valid @RequestBody ApplyCouponRequest request) {
        return mapper.toApplicationResponse(couponUseCases.applyCoupon(
                request.code(), SecurityUtils.requiredUserId(), mapper.toApplyCommand(request)));
    }
}