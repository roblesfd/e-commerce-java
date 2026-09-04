package org.fernandodev.ecommerce.wishlist.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.fernandodev.ecommerce.wishlist.adapter.in.rest.dto.AddItemRequest;
import org.fernandodev.ecommerce.wishlist.adapter.in.rest.dto.CreateWishlistRequest;
import org.fernandodev.ecommerce.wishlist.adapter.in.rest.dto.WishlistResponse;
import org.fernandodev.ecommerce.wishlist.adapter.in.rest.mapper.WishlistMapper;
import org.fernandodev.ecommerce.wishlist.application.port.in.WishlistUseCases;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST encargado de exponer los casos de uso relacionados
 * con la gestión de wishlists del contexto correspondiente.
 *
 * <p>Este controlador actúa como adaptador de entrada HTTP y delega
 * la ejecución de las operaciones en {@link WishlistUseCases},
 * manteniendo la lógica de negocio fuera de la capa de infraestructura.</p>
 *
 * <p>La identidad del cliente autenticado se obtiene mediante
 * {@code SecurityUtils}, mientras que {@link WishlistMapper} se utiliza
 * para realizar las conversiones entre los modelos de la API y los
 * modelos utilizados por la aplicación.</p>
 */
@RestController
@RequestMapping("/api/v1/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    /**
     * Puerto de entrada que expone los casos de uso relacionados
     * con la gestión de wishlists.
     */
    private final WishlistUseCases wishlistUseCases;

    /**
     * Mapper encargado de convertir entre los modelos de la API
     * y los modelos utilizados por la aplicación.
     */
    private final WishlistMapper mapper;

    /**
     * Crea una nueva wishlist para el cliente autenticado.
     *
     * <p>El propietario de la wishlist se obtiene del contexto de
     * seguridad mediante {@code SecurityUtils}, evitando que el cliente
     * pueda indicar arbitrariamente el identificador de otro usuario.</p>
     *
     * @param request datos necesarios para crear la wishlist.
     * @return representación de la wishlist creada.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistResponse create(
            @Valid @RequestBody CreateWishlistRequest request
    ) {
        return mapper.toResponse(
                wishlistUseCases.create(
                        SecurityUtils.requiredUserId(),
                        request.name()
                )
        );
    }

    /**
     * Obtiene las wishlists pertenecientes al cliente autenticado.
     *
     * <p>El identificador del cliente se obtiene directamente del
     * contexto de seguridad y no se recibe como parámetro HTTP.</p>
     *
     * @return lista de wishlists pertenecientes al cliente autenticado.
     */
    @GetMapping
    public List<WishlistResponse> mine() {
        return wishlistUseCases
                .listByCustomer(SecurityUtils.requiredUserId())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Obtiene una wishlist a partir de su identificador.
     *
     * @param id identificador único de la wishlist.
     * @return representación de la wishlist solicitada.
     */
    @GetMapping("/{id}")
    public WishlistResponse get(@PathVariable UUID id) {
        return mapper.toResponse(
                wishlistUseCases.getById(
                        WishlistId.of(id)
                )
        );
    }

    /**
     * Cambia el nombre de una wishlist existente.
     *
     * <p>El nuevo nombre es validado mediante {@link Valid} antes
     * de ser enviado al caso de uso correspondiente.</p>
     *
     * @param id identificador único de la wishlist.
     * @param request solicitud que contiene el nuevo nombre.
     * @return representación actualizada de la wishlist.
     */
    @PatchMapping("/{id}")
    public WishlistResponse rename(
            @PathVariable UUID id,
            @Valid @RequestBody CreateWishlistRequest request
    ) {
        return mapper.toResponse(
                wishlistUseCases.rename(
                        WishlistId.of(id),
                        request.name()
                )
        );
    }

    /**
     * Elimina una wishlist.
     *
     * <p>La operación no devuelve contenido en la respuesta HTTP
     * cuando la eliminación se completa correctamente.</p>
     *
     * @param id identificador único de la wishlist que se desea eliminar.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        wishlistUseCases.delete(WishlistId.of(id));
    }

    /**
     * Agrega un producto o variante a una wishlist.
     *
     * <p>Los datos del elemento recibido se transforman mediante
     * {@link WishlistMapper} antes de ser enviados al caso de uso.
     * Las reglas de negocio relacionadas con duplicados son
     * responsabilidad del agregado y de la capa de aplicación.</p>
     *
     * @param id identificador único de la wishlist.
     * @param request datos del producto o variante que se desea agregar.
     * @return representación actualizada de la wishlist.
     */
    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistResponse addItem(
            @PathVariable UUID id,
            @Valid @RequestBody AddItemRequest request
    ) {
        return mapper.toResponse(
                wishlistUseCases.addItem(
                        WishlistId.of(id),
                        mapper.toCommand(request)
                )
        );
    }

    /**
     * Elimina un producto o variante de una wishlist.
     *
     * <p>El producto se identifica mediante {@code productId}. El
     * {@code variantId} es opcional y permite distinguir entre un
     * producto general y una variante específica.</p>
     *
     * <p>Cuando {@code variantId} no se proporciona, se utiliza
     * {@code null} para indicar que la operación corresponde al
     * producto sin una variante específica.</p>
     *
     * @param id identificador único de la wishlist.
     * @param productId identificador del producto que se desea eliminar.
     * @param variantId identificador opcional de la variante del producto.
     */
    @DeleteMapping("/{id}/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(
            @PathVariable UUID id,
            @PathVariable UUID productId,
            @RequestParam(required = false) UUID variantId
    ) {
        wishlistUseCases.removeItem(
                WishlistId.of(id),
                ProductId.of(productId),
                variantId == null
                        ? null
                        : ProductVariantId.of(variantId)
        );
    }

    /**
     * Elimina todos los elementos de una wishlist.
     *
     * <p>La wishlist no es eliminada; únicamente se eliminan todos
     * los productos y variantes que contiene.</p>
     *
     * @param id identificador único de la wishlist que se desea vaciar.
     */
    @DeleteMapping("/{id}/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@PathVariable UUID id) {
        wishlistUseCases.clear(WishlistId.of(id));
    }
}
