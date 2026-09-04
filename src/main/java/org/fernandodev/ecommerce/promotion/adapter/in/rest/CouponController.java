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
 * Controlador REST del contexto Promotions &amp; Coupons encargado de exponer
 * las operaciones relacionadas con la gestión de cupones.
 *
 * <p>Permite crear, consultar, actualizar, activar, desactivar, validar y
 * aplicar cupones mediante una API REST.</p>
 *
 * <p>El controlador delega la ejecución de las operaciones a
 * {@link CouponUseCases} y utiliza {@link CouponMapper} para transformar
 * los objetos de entrada y salida de la API.</p>
 */
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    /**
     * Puerto de entrada que expone los casos de uso relacionados con
     * la gestión de cupones.
     */
    private final CouponUseCases couponUseCases;

    /**
     * Componente encargado de transformar los objetos de la API REST
     * en comandos de aplicación y los resultados de los casos de uso
     * en respuestas REST.
     */
    private final CouponMapper mapper;

    /**
     * Crea un nuevo cupón.
     *
     * <p>La solicitud contiene la información necesaria para crear el cupón.
     * El controlador transforma dicha información en un comando y delega
     * la operación al caso de uso correspondiente.</p>
     *
     * @param request datos necesarios para crear el cupón.
     * @return representación REST del cupón creado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse create(
            @Valid @RequestBody CouponRequest request
    ) {
        return mapper.toResponse(
                couponUseCases.create(
                        mapper.toCreateCommand(request)
                )
        );
    }

    /**
     * Obtiene un cupón mediante su identificador único.
     *
     * @param id identificador único del cupón.
     * @return representación REST del cupón solicitado.
     */
    @GetMapping("/{id}")
    public CouponResponse get(@PathVariable UUID id) {
        return mapper.toResponse(
                couponUseCases.getById(
                        CouponId.of(id)
                )
        );
    }

    /**
     * Obtiene todos los cupones disponibles.
     *
     * @return lista de cupones representados mediante objetos de respuesta REST.
     */
    @GetMapping
    public List<CouponResponse> list() {
        return couponUseCases.list()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Actualiza un cupón existente.
     *
     * <p>El identificador del cupón se obtiene de la ruta y los datos
     * actualizados se reciben mediante el cuerpo de la solicitud.</p>
     *
     * @param id identificador único del cupón que se desea actualizar.
     * @param request datos con los nuevos valores del cupón.
     * @return representación REST del cupón actualizado.
     */
    @PutMapping("/{id}")
    public CouponResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCouponRequest request
    ) {
        return mapper.toResponse(
                couponUseCases.update(
                        CouponId.of(id),
                        mapper.toUpdateCommand(request)
                )
        );
    }

    /**
     * Activa un cupón.
     *
     * <p>Una vez activado, el cupón podrá considerarse válido siempre que
     * cumpla las demás condiciones de utilización y se encuentre dentro
     * de su periodo de vigencia.</p>
     *
     * @param id identificador único del cupón que se desea activar.
     * @return representación REST del cupón activado.
     */
    @PatchMapping("/{id}/activate")
    public CouponResponse activate(@PathVariable UUID id) {
        return mapper.toResponse(
                couponUseCases.activate(
                        CouponId.of(id)
                )
        );
    }

    /**
     * Desactiva un cupón.
     *
     * <p>Un cupón desactivado no podrá considerarse válido aunque se
     * encuentre dentro de su periodo de vigencia.</p>
     *
     * @param id identificador único del cupón que se desea desactivar.
     * @return representación REST del cupón desactivado.
     */
    @PatchMapping("/{id}/deactivate")
    public CouponResponse deactivate(@PathVariable UUID id) {
        return mapper.toResponse(
                couponUseCases.deactivate(
                        CouponId.of(id)
                )
        );
    }

    /**
     * Valida un cupón sin aplicarlo.
     *
     * <p>La operación determina si el cupón puede utilizarse por el usuario
     * autenticado bajo las condiciones proporcionadas en la solicitud.</p>
     *
     * <p>El identificador del usuario se obtiene del contexto de seguridad
     * y no es proporcionado directamente por el cliente.</p>
     *
     * @param request datos necesarios para validar el cupón.
     * @return resultado de la validación del cupón.
     */
    @PostMapping("/validate")
    public CouponValidationResponse validate(
            @Valid @RequestBody ApplyCouponRequest request
    ) {
        return mapper.toValidationResponse(
                couponUseCases.validateCoupon(
                        request.code(),
                        SecurityUtils.requiredUserId(),
                        mapper.toApplyCommand(request)
                )
        );
    }

    /**
     * Aplica un cupón al usuario autenticado.
     *
     * <p>La operación delega en el caso de uso correspondiente la aplicación
     * del cupón utilizando su código y las condiciones especificadas en
     * la solicitud.</p>
     *
     * <p>El identificador del usuario se obtiene del contexto de seguridad
     * mediante {@link SecurityUtils#requiredUserId()}.</p>
     *
     * @param request datos necesarios para aplicar el cupón.
     * @return resultado de la aplicación del cupón.
     */
    @PostMapping("/apply")
    public CouponApplicationResponse apply(
            @Valid @RequestBody ApplyCouponRequest request
    ) {
        return mapper.toApplicationResponse(
                couponUseCases.applyCoupon(
                        request.code(),
                        SecurityUtils.requiredUserId(),
                        mapper.toApplyCommand(request)
                )
        );
    }
}

