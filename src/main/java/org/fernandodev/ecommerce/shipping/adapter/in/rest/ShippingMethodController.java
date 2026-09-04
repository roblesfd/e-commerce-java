package org.fernandodev.ecommerce.shipping.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.ShippingMethodRequest;
import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.ShippingMethodResponse;
import org.fernandodev.ecommerce.shipping.adapter.in.rest.mapper.ShippingMethodMapper;
import org.fernandodev.ecommerce.shipping.application.port.in.ShippingMethodUseCases;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST encargado de exponer los casos de uso relacionados
 * con la gestión de los métodos de envío del contexto {@code Shipping}.
 *
 * <p>Este controlador actúa como adaptador de entrada HTTP y delega
 * las operaciones en {@link ShippingMethodUseCases}, manteniendo la
 * lógica de negocio fuera de la capa de infraestructura.</p>
 *
 * <p>La conversión entre los objetos utilizados por la API y los
 * modelos de la aplicación se realiza mediante {@link ShippingMethodMapper}.</p>
 */
@RestController
@RequestMapping("/api/v1/shipping-methods")
@RequiredArgsConstructor
public class ShippingMethodController {

    /**
     * Puerto de entrada que expone los casos de uso relacionados
     * con los métodos de envío.
     */
    private final ShippingMethodUseCases shippingMethodUseCases;

    /**
     * Mapper encargado de convertir entre los modelos de la API
     * y los modelos utilizados por la aplicación.
     */
    private final ShippingMethodMapper mapper;

    /**
     * Crea un nuevo método de envío.
     *
     * <p>Los datos recibidos en la solicitud son validados mediante
     * {@link Valid} y posteriormente transformados a un comando
     * de aplicación mediante {@link ShippingMethodMapper}.</p>
     *
     * @param request datos necesarios para crear el método de envío.
     * @return representación del método de envío creado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShippingMethodResponse create(
            @Valid @RequestBody ShippingMethodRequest request
    ) {
        return mapper.toResponse(
                shippingMethodUseCases.create(
                        mapper.toCommand(request)
                )
        );
    }

    /**
     * Obtiene todos los métodos de envío registrados.
     *
     * <p>El resultado de cada método de envío es transformado a su
     * representación correspondiente para la API.</p>
     *
     * @return lista de métodos de envío disponibles en el sistema.
     */
    @GetMapping
    public List<ShippingMethodResponse> list() {
        return shippingMethodUseCases
                .list()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Obtiene un método de envío a partir de su identificador.
     *
     * @param id identificador único del método de envío.
     * @return representación del método de envío solicitado.
     */
    @GetMapping("/{id}")
    public ShippingMethodResponse get(@PathVariable UUID id) {
        return mapper.toResponse(
                shippingMethodUseCases.getById(
                        ShippingMethodId.of(id)
                )
        );
    }

    /**
     * Activa un método de envío.
     *
     * <p>Una vez activado, el método de envío puede volver a estar
     * disponible para las operaciones que permitan seleccionarlo.</p>
     *
     * @param id identificador único del método de envío.
     * @return representación actualizada del método de envío.
     */
    @PatchMapping("/{id}/activate")
    public ShippingMethodResponse activate(@PathVariable UUID id) {
        return mapper.toResponse(
                shippingMethodUseCases.activate(
                        ShippingMethodId.of(id)
                )
        );
    }

    /**
     * Desactiva un método de envío.
     *
     * <p>La desactivación conserva el método registrado en el sistema,
     * pero evita que sea considerado disponible para nuevas operaciones
     * que requieran seleccionar un método de envío.</p>
     *
     * @param id identificador único del método de envío.
     * @return representación actualizada del método de envío.
     */
    @PatchMapping("/{id}/deactivate")
    public ShippingMethodResponse deactivate(@PathVariable UUID id) {
        return mapper.toResponse(
                shippingMethodUseCases.deactivate(
                        ShippingMethodId.of(id)
                )
        );
    }
}
