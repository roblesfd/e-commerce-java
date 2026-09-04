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
 * Controlador REST del contexto Promotions &amp; Coupons encargado de exponer
 * las operaciones relacionadas con la gestión de promociones.
 *
 * <p>Permite crear, consultar, actualizar, activar, desactivar promociones,
 * así como administrar los productos y categorías asociados y registrar
 * el uso de una promoción.</p>
 *
 * <p>El controlador delega la ejecución de los casos de uso a
 * {@link PromotionUseCases} y utiliza {@link PromotionMapper} para realizar
 * las conversiones entre los modelos de la API y los modelos utilizados
 * por la aplicación.</p>
 */
@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    /**
     * Puerto de entrada que expone los casos de uso relacionados con
     * la gestión de promociones.
     */
    private final PromotionUseCases promotionUseCases;

    /**
     * Componente encargado de realizar las conversiones entre los objetos
     * utilizados por la capa REST y los modelos de la aplicación.
     */
    private final PromotionMapper mapper;

    /**
     * Crea una nueva promoción.
     *
     * <p>Recibe los datos de la promoción mediante el cuerpo de la petición,
     * los transforma en un comando de creación y delega la operación al
     * caso de uso correspondiente.</p>
     *
     * @param request datos necesarios para crear la promoción.
     * @return representación REST de la promoción creada.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionResponse create(
            @Valid @RequestBody PromotionRequest request
    ) {
        return mapper.toResponse(
                promotionUseCases.create(
                        mapper.toCreateCommand(request)
                )
        );
    }

    /**
     * Obtiene una promoción a partir de su identificador.
     *
     * @param id identificador único de la promoción.
     * @return representación REST de la promoción solicitada.
     */
    @GetMapping("/{id}")
    public PromotionResponse get(@PathVariable UUID id) {
        return mapper.toResponse(
                promotionUseCases.getById(
                        PromotionId.of(id)
                )
        );
    }

    /**
     * Obtiene la lista de promociones disponibles.
     *
     * @return lista de promociones representadas mediante objetos de respuesta
     *         REST.
     */
    @GetMapping
    public List<PromotionResponse> list() {
        return promotionUseCases.list()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Actualiza los datos de una promoción existente.
     *
     * <p>El identificador de la promoción se obtiene de la URL y los nuevos
     * datos se reciben mediante el cuerpo de la petición.</p>
     *
     * @param id identificador único de la promoción que se desea actualizar.
     * @param request datos con los nuevos valores de la promoción.
     * @return representación REST de la promoción actualizada.
     */
    @PutMapping("/{id}")
    public PromotionResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePromotionRequest request
    ) {
        return mapper.toResponse(
                promotionUseCases.update(
                        PromotionId.of(id),
                        mapper.toUpdateCommand(request)
                )
        );
    }

    /**
     * Activa una promoción.
     *
     * <p>La operación habilita la promoción para que pueda participar en
     * las operaciones de aplicación correspondientes, siempre que cumpla
     * las demás reglas de negocio.</p>
     *
     * @param id identificador único de la promoción que se desea activar.
     * @return representación REST de la promoción activada.
     */
    @PatchMapping("/{id}/activate")
    public PromotionResponse activate(@PathVariable UUID id) {
        return mapper.toResponse(
                promotionUseCases.activate(
                        PromotionId.of(id)
                )
        );
    }

    /**
     * Desactiva una promoción.
     *
     * <p>La operación impide que la promoción pueda utilizarse mientras
     * permanezca desactivada.</p>
     *
     * @param id identificador único de la promoción que se desea desactivar.
     * @return representación REST de la promoción desactivada.
     */
    @PatchMapping("/{id}/deactivate")
    public PromotionResponse deactivate(@PathVariable UUID id) {
        return mapper.toResponse(
                promotionUseCases.deactivate(
                        PromotionId.of(id)
                )
        );
    }

    /**
     * Asocia un producto a una promoción.
     *
     * <p>El producto se identifica mediante su identificador y se agrega
     * a los productos asociados con la promoción indicada.</p>
     *
     * @param id identificador único de la promoción.
     * @param productId identificador único del producto que se desea asociar.
     * @return representación REST de la promoción con el producto asociado.
     */
    @PostMapping("/{id}/products")
    public PromotionResponse addProduct(
            @PathVariable UUID id,
            @RequestBody UUID productId
    ) {
        return mapper.toResponse(
                promotionUseCases.addProduct(
                        PromotionId.of(id),
                        ProductId.of(productId)
                )
        );
    }

    /**
     * Elimina un producto de una promoción.
     *
     * <p>La operación elimina la asociación entre la promoción y el producto
     * especificados.</p>
     *
     * @param id identificador único de la promoción.
     * @param productId identificador único del producto que se desea eliminar.
     * @return representación REST de la promoción después de eliminar
     *         la asociación.
     */
    @DeleteMapping("/{id}/products/{productId}")
    public PromotionResponse removeProduct(
            @PathVariable UUID id,
            @PathVariable UUID productId
    ) {
        return mapper.toResponse(
                promotionUseCases.removeProduct(
                        PromotionId.of(id),
                        ProductId.of(productId)
                )
        );
    }

    /**
     * Asocia una categoría a una promoción.
     *
     * <p>La categoría se identifica mediante su identificador y se agrega
     * a las categorías asociadas con la promoción indicada.</p>
     *
     * @param id identificador único de la promoción.
     * @param categoryId identificador único de la categoría que se desea asociar.
     * @return representación REST de la promoción con la categoría asociada.
     */
    @PostMapping("/{id}/categories")
    public PromotionResponse addCategory(
            @PathVariable UUID id,
            @RequestBody UUID categoryId
    ) {
        return mapper.toResponse(
                promotionUseCases.addCategory(
                        PromotionId.of(id),
                        CategoryId.of(categoryId)
                )
        );
    }

    /**
     * Elimina una categoría de una promoción.
     *
     * <p>La operación elimina la asociación entre la promoción y la categoría
     * especificadas.</p>
     *
     * @param id identificador único de la promoción.
     * @param categoryId identificador único de la categoría que se desea eliminar.
     * @return representación REST de la promoción después de eliminar
     *         la asociación.
     */
    @DeleteMapping("/{id}/categories/{categoryId}")
    public PromotionResponse removeCategory(
            @PathVariable UUID id,
            @PathVariable UUID categoryId
    ) {
        return mapper.toResponse(
                promotionUseCases.removeCategory(
                        PromotionId.of(id),
                        CategoryId.of(categoryId)
                )
        );
    }

    /**
     * Registra el uso de una promoción por parte del usuario autenticado.
     *
     * <p>El identificador del usuario se obtiene del contexto de seguridad
     * mediante {@link SecurityUtils#requiredUserId()}, evitando que dicho
     * identificador sea proporcionado directamente por el cliente.</p>
     *
     * @param id identificador único de la promoción cuyo uso se desea registrar.
     * @return representación REST de la promoción después de registrar su uso.
     */
    @PostMapping("/{id}/usage")
    public PromotionResponse recordUsage(@PathVariable UUID id) {
        return mapper.toResponse(
                promotionUseCases.recordUsage(
                        PromotionId.of(id),
                        SecurityUtils.requiredUserId()
                )
        );
    }
}
