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
 * Controlador REST para la gestión de reviews y ratings de productos.
 *
 * <p>Expone los endpoints necesarios para crear, consultar, actualizar
 * y eliminar reviews, así como para gestionar su ciclo de moderación,
 * respuestas y valoración agregada de los productos.</p>
 *
 * <p>El controlador delega la ejecución de las operaciones de negocio
 * en {@link ReviewUseCases} y utiliza los mappers correspondientes para
 * convertir entre los modelos de la API y los modelos del dominio.</p>
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    /**
     * Casos de uso relacionados con la gestión de reviews.
     */
    private final ReviewUseCases reviewUseCases;

    /**
     * Mapper encargado de convertir entre requests/responses de la API
     * y los modelos utilizados por los casos de uso de reviews.
     */
    private final ReviewMapper mapper;

    /**
     * Mapper encargado de convertir el resumen de ratings del dominio
     * en la respuesta expuesta por la API.
     */
    private final RatingSummaryMapper ratingSummaryMapper;

    /**
     * Crea una nueva review asociada al producto indicado.
     *
     * <p>El identificador del usuario se obtiene del contexto de seguridad
     * actual, por lo que no es necesario recibirlo como parte de la petición.</p>
     *
     * @param request datos necesarios para crear la review.
     * @return representación de la review creada.
     */
    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@Valid @RequestBody CreateReviewRequest request) {
        return mapper.toResponse(reviewUseCases.create(
                SecurityUtils.requiredUserId(),
                ProductId.of(request.productId()),
                mapper.toCreateCommand(request)));
    }

    /**
     * Actualiza una review existente.
     *
     * @param id identificador único de la review.
     * @param request datos que contienen los nuevos valores de la review.
     * @return representación actualizada de la review.
     */
    @PutMapping("/reviews/{id}")
    public ReviewResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReviewRequest request) {

        return mapper.toResponse(
                reviewUseCases.update(
                        ReviewId.of(id),
                        mapper.toUpdateCommand(request)));
    }

    /**
     * Elimina una review existente.
     *
     * @param id identificador único de la review que se desea eliminar.
     */
    @DeleteMapping("/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        reviewUseCases.delete(ReviewId.of(id));
    }

    /**
     * Obtiene una review a partir de su identificador.
     *
     * @param id identificador único de la review.
     * @return representación de la review solicitada.
     */
    @GetMapping("/reviews/{id}")
    public ReviewResponse get(@PathVariable UUID id) {
        return mapper.toResponse(
                reviewUseCases.getById(ReviewId.of(id)));
    }

    /**
     * Obtiene las reviews creadas por el usuario autenticado.
     *
     * <p>El identificador del usuario se obtiene directamente del
     * contexto de seguridad actual.</p>
     *
     * @return lista de reviews pertenecientes al usuario autenticado.
     */
    @GetMapping("/reviews/me")
    public List<ReviewResponse> mine() {
        return reviewUseCases.listByCustomer(SecurityUtils.requiredUserId()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Obtiene las reviews disponibles para el proceso de moderación.
     *
     * <p>El parámetro {@code status} permite indicar un estado de review
     * como filtro de la consulta. Actualmente, la ejecución del caso de
     * uso se delega en {@link ReviewUseCases#listForModeration()}.</p>
     *
     * @param status estado por el que se desea filtrar las reviews;
     *               puede ser {@code null} si no se especifica.
     * @return lista de reviews disponibles para moderación.
     */
    @GetMapping("/reviews")
    public List<ReviewResponse> moderation(
            @RequestParam(required = false) ReviewStatus status) {

        return reviewUseCases.listForModeration().stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Aprueba una review durante el proceso de moderación.
     *
     * <p>La aprobación permite que la review avance al estado
     * correspondiente para su publicación.</p>
     *
     * @param id identificador único de la review que se desea aprobar.
     * @return representación de la review después de la aprobación.
     */
    @PostMapping("/reviews/{id}/approve")
    public ReviewResponse approve(@PathVariable UUID id) {
        return mapper.toResponse(
                reviewUseCases.approve(ReviewId.of(id)));
    }

    /**
     * Rechaza una review durante el proceso de moderación.
     *
     * @param id identificador único de la review que se desea rechazar.
     * @return representación de la review después del rechazo.
     */
    @PostMapping("/reviews/{id}/reject")
    public ReviewResponse reject(@PathVariable UUID id) {
        return mapper.toResponse(
                reviewUseCases.reject(ReviewId.of(id)));
    }

    /**
     * Oculta una review para impedir que sea mostrada públicamente.
     *
     * <p>La operación no implica necesariamente la eliminación de la
     * review, sino un cambio en su visibilidad dentro del sistema.</p>
     *
     * @param id identificador único de la review que se desea ocultar.
     * @return representación de la review después de ocultarla.
     */
    @PostMapping("/reviews/{id}/hide")
    public ReviewResponse hide(@PathVariable UUID id) {
        return mapper.toResponse(
                reviewUseCases.hide(ReviewId.of(id)));
    }

    /**
     * Restaura una review que se encontraba oculta.
     *
     * @param id identificador único de la review que se desea restaurar.
     * @return representación de la review después de su restauración.
     */
    @PostMapping("/reviews/{id}/restore")
    public ReviewResponse restore(@PathVariable UUID id) {
        return mapper.toResponse(
                reviewUseCases.restore(ReviewId.of(id)));
    }

    /**
     * Agrega una respuesta a una review existente.
     *
     * <p>El usuario que realiza la respuesta se obtiene del contexto
     * de seguridad actual.</p>
     *
     * @param id identificador único de la review a la que se agregará
     *           la respuesta.
     * @param request datos que contienen el comentario de la respuesta.
     * @return representación de la review después de agregar la respuesta.
     */
    @PostMapping("/reviews/{id}/replies")
    public ReviewResponse addReply(
            @PathVariable UUID id,
            @Valid @RequestBody AddReplyRequest request) {

        return mapper.toResponse(
                reviewUseCases.addReply(
                        ReviewId.of(id),
                        SecurityUtils.requiredUserId(),
                        request.comment()));
    }

    /**
     * Obtiene las reviews asociadas a un producto.
     *
     * @param productId identificador único del producto.
     * @return lista de reviews asociadas al producto.
     */
    @GetMapping("/products/{productId}/reviews")
    public List<ReviewResponse> listByProduct(
            @PathVariable UUID productId) {

        return reviewUseCases.listByProduct(ProductId.of(productId)).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Obtiene el resumen de ratings de un producto.
     *
     * <p>El resumen normalmente contiene información agregada de las
     * valoraciones, como el rating promedio y su distribución.</p>
     *
     * @param productId identificador único del producto.
     * @return resumen de las valoraciones del producto.
     */
    @GetMapping("/products/{productId}/rating")
    public RatingSummaryResponse rating(@PathVariable UUID productId) {
        return ratingSummaryMapper.toResponse(
                reviewUseCases.ratingSummary(ProductId.of(productId)));
    }
}
