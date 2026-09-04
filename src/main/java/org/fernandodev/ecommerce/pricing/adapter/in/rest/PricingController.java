package org.fernandodev.ecommerce.pricing.adapter.in.rest;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.adapter.in.rest.dto.AddOverrideRequest;
import org.fernandodev.ecommerce.pricing.adapter.in.rest.dto.CurrentPriceResponse;
import org.fernandodev.ecommerce.pricing.adapter.in.rest.dto.DefinePriceRequest;
import org.fernandodev.ecommerce.pricing.adapter.in.rest.dto.PriceResponse;
import org.fernandodev.ecommerce.pricing.adapter.in.rest.dto.PriceSnapshotResponse;
import org.fernandodev.ecommerce.pricing.adapter.in.rest.dto.UpdatePriceRequest;
import org.fernandodev.ecommerce.pricing.adapter.in.rest.mapper.PricingMapper;
import org.fernandodev.ecommerce.pricing.application.port.in.PricingUseCases;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceOverrideId;
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
 * Controlador REST del contexto {@code Pricing}.
 *
 * <p>Expone los endpoints HTTP utilizados para definir, consultar y modificar
 * los precios de productos y variantes, así como para gestionar sus
 * modificaciones de precio (overrides).</p>
 *
 * <p>El controlador se limita a adaptar las solicitudes HTTP a los casos de
 * uso de la aplicación y sus resultados a respuestas REST. No contiene
 * lógica de negocio.</p>
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PricingController {

    private final PricingUseCases pricingUseCases;
    private final PricingMapper mapper;

    /**
     * Define el precio base de un producto o de una variante del producto.
     *
     * @param productId identificador único del producto al que pertenece el precio
     * @param request datos necesarios para definir el precio
     * @return representación REST del precio creado
     */
    @PostMapping("/products/{productId}/prices")
    @ResponseStatus(HttpStatus.CREATED)
    public PriceResponse defineBasePrice(@PathVariable UUID productId,
                                         @Valid @RequestBody DefinePriceRequest request) {
        return mapper.toResponse(
                pricingUseCases.defineBasePrice(
                        mapper.toCommand(productId, request)
                )
        );
    }

    /**
     * Obtiene el precio vigente de un producto o de una variante en un
     * instante determinado.
     *
     * @param productId identificador único del producto
     * @param variantId identificador de la variante cuyo precio se desea
     *                 consultar; puede omitirse para consultar el precio
     *                 correspondiente al producto
     * @return respuesta REST con el precio vigente y el instante de consulta
     */
    @GetMapping("/products/{productId}/prices/current")
    public CurrentPriceResponse currentPrice(@PathVariable UUID productId,
                                             @RequestParam(required = false) UUID variantId) {
        Instant at = Instant.now();

        return mapper.toCurrentPriceResponse(
                pricingUseCases.currentPrice(
                        ProductId.of(productId),
                        variant(variantId),
                        at
                ),
                at
        );
    }

    /**
     * Obtiene una instantánea del estado de precios de un producto o variante
     * en el momento de la consulta.
     *
     * @param productId identificador único del producto
     * @param variantId identificador de la variante; puede omitirse para
     *                 consultar el precio del producto
     * @return respuesta REST que representa la instantánea del precio
     */
    @GetMapping("/products/{productId}/prices/snapshot")
    public PriceSnapshotResponse priceSnapshot(@PathVariable UUID productId,
                                               @RequestParam(required = false) UUID variantId) {
        return mapper.toSnapshotResponse(
                pricingUseCases.priceSnapshot(
                        ProductId.of(productId),
                        variant(variantId),
                        Instant.now()
                )
        );
    }

    /**
     * Obtiene un precio a partir de su identificador.
     *
     * @param priceId identificador único del precio
     * @return representación REST del precio solicitado
     */
    @GetMapping("/prices/{priceId}")
    public PriceResponse getById(@PathVariable UUID priceId) {
        return mapper.toResponse(
                pricingUseCases.getById(PriceId.of(priceId))
        );
    }

    /**
     * Actualiza el precio base asociado a un precio existente.
     *
     * @param priceId identificador único del precio que será actualizado
     * @param request datos del nuevo precio base
     * @return representación REST del precio actualizado
     */
    @PutMapping("/prices/{priceId}")
    public PriceResponse updateBasePrice(@PathVariable UUID priceId,
                                         @Valid @RequestBody UpdatePriceRequest request) {
        return mapper.toResponse(
                pricingUseCases.updateBasePrice(
                        PriceId.of(priceId),
                        mapper.toCommand(request)
                )
        );
    }

    /**
     * Agrega una modificación de precio (override) a un precio existente.
     *
     * <p>Un override permite definir un precio alternativo durante un período
     * de vigencia determinado o bajo el tipo de modificación especificado.</p>
     *
     * @param priceId identificador único del precio al que se agregará el override
     * @param request datos del override, incluyendo importe, moneda y período
     *                de vigencia
     * @return representación REST del precio con el nuevo override agregado
     */
    @PostMapping("/prices/{priceId}/overrides")
    @ResponseStatus(HttpStatus.CREATED)
    public PriceResponse addOverride(@PathVariable UUID priceId,
                                     @Valid @RequestBody AddOverrideRequest request) {
        return mapper.toResponse(
                pricingUseCases.addOverride(
                        PriceId.of(priceId),
                        mapper.toCommand(request)
                )
        );
    }

    /**
     * Elimina un override asociado a un precio.
     *
     * @param priceId identificador único del precio al que pertenece el override
     * @param overrideId identificador único del override que será eliminado
     */
    @DeleteMapping("/prices/{priceId}/overrides/{overrideId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeOverride(@PathVariable UUID priceId,
                               @PathVariable UUID overrideId) {
        pricingUseCases.removeOverride(
                PriceId.of(priceId),
                PriceOverrideId.of(overrideId)
        );
    }

    /**
     * Convierte un identificador de variante opcional a su correspondiente
     * identificador del dominio.
     *
     * @param variantId identificador de la variante; puede ser {@code null}
     * @return {@link ProductVariantId} correspondiente o {@code null} cuando
     *         no se proporcionó una variante
     */
    private static ProductVariantId variant(UUID variantId) {
        return variantId == null ? null : ProductVariantId.of(variantId);
    }
}
