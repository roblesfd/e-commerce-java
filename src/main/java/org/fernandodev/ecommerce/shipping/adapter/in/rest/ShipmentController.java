package org.fernandodev.ecommerce.shipping.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.CreateShipmentRequest;
import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.ShipmentResponse;
import org.fernandodev.ecommerce.shipping.adapter.in.rest.mapper.ShipmentMapper;
import org.fernandodev.ecommerce.shipping.application.port.in.ShipmentUseCases;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentId;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST encargado de exponer los casos de uso relacionados
 * con la gestión de envíos del contexto {@code Shipping}.
 *
 * <p>Este controlador actúa como adaptador de entrada HTTP y delega
 * la ejecución de las operaciones en {@link ShipmentUseCases}.
 * No contiene lógica de negocio propia; su responsabilidad se limita
 * a recibir las solicitudes HTTP, convertir sus datos al modelo
 * utilizado por la aplicación y transformar los resultados en
 * respuestas HTTP.</p>
 *
 * <p>Las operaciones relacionadas con la identidad del cliente utilizan
 * {@code SecurityUtils} para obtener el identificador del usuario
 * autenticado, evitando recibir dicha información directamente
 * desde el cliente.</p>
 */
@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    /**
     * Puerto de entrada que expone los casos de uso relacionados
     * con la gestión de envíos.
     */
    private final ShipmentUseCases shipmentUseCases;

    /**
     * Mapper encargado de convertir entre los modelos utilizados
     * por la capa HTTP y los modelos de la aplicación.
     */
    private final ShipmentMapper mapper;

    /**
     * Crea un nuevo envío asociado a una orden.
     *
     * <p>La orden se identifica mediante el {@code orderId} recibido
     * en la solicitud y el resto de los datos necesarios para crear
     * el envío se transforman mediante {@link ShipmentMapper}.</p>
     *
     * @param request datos necesarios para crear el envío.
     * @return representación del envío creado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse create(
            @Valid @RequestBody CreateShipmentRequest request
    ) {
        return mapper.toResponse(
                shipmentUseCases.create(
                        OrderId.of(request.orderId()),
                        mapper.toCommand(request)
                )
        );
    }

    /**
     * Obtiene un envío a partir de su identificador.
     *
     * @param id identificador único del envío.
     * @return representación del envío solicitado.
     */
    @GetMapping("/{id}")
    public ShipmentResponse get(@PathVariable UUID id) {
        return mapper.toResponse(
                shipmentUseCases.getById(ShipmentId.of(id))
        );
    }

    /**
     * Obtiene los envíos asociados al cliente autenticado.
     *
     * <p>El identificador del cliente se obtiene directamente del
     * contexto de seguridad mediante {@code SecurityUtils}, por lo
     * que no es necesario recibirlo como parámetro de la solicitud.</p>
     *
     * @return lista de envíos pertenecientes al cliente autenticado.
     */
    @GetMapping("/me")
    public List<ShipmentResponse> mine() {
        return shipmentUseCases
                .listByCustomer(SecurityUtils.requiredUserId())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Obtiene el envío asociado a una orden.
     *
     * <p>Si no existe un envío asociado a la orden indicada, se lanza
     * {@link ResourceNotFoundException} para indicar que el recurso
     * solicitado no fue encontrado.</p>
     *
     * @param orderId identificador único de la orden.
     * @return representación del envío asociado a la orden.
     * @throws ResourceNotFoundException si no existe un envío para
     *         la orden indicada.
     */
    @GetMapping
    public ShipmentResponse getByOrder(@RequestParam UUID orderId) {
        return shipmentUseCases
                .getByOrder(OrderId.of(orderId))
                .map(mapper::toResponse)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Envío no encontrado para la orden: " + orderId
                        )
                );
    }

    /**
     * Marca un envío como preparado.
     *
     * <p>Esta operación inicia la transición del envío desde
     * {@code PENDING} hacia {@code PREPARING}, de acuerdo con las
     * reglas de transición definidas por el agregado {@code Shipment}.</p>
     *
     * @param id identificador único del envío.
     * @return representación actualizada del envío.
     */
    @PostMapping("/{id}/prepared")
    public ShipmentResponse markPrepared(@PathVariable UUID id) {
        return mapper.toResponse(
                shipmentUseCases.markPrepared(ShipmentId.of(id))
        );
    }

    /**
     * Marca un envío como despachado.
     *
     * <p>La operación representa la salida del envío del proceso de
     * preparación y su entrega al carrier para iniciar el transporte.</p>
     *
     * @param id identificador único del envío.
     * @return representación actualizada del envío.
     */
    @PostMapping("/{id}/shipped")
    public ShipmentResponse markShipped(@PathVariable UUID id) {
        return mapper.toResponse(
                shipmentUseCases.markShipped(ShipmentId.of(id))
        );
    }

    /**
     * Marca un envío como actualmente en tránsito.
     *
     * <p>Esta operación representa el avance del envío después de haber
     * sido despachado por el carrier.</p>
     *
     * @param id identificador único del envío.
     * @return representación actualizada del envío.
     */
    @PostMapping("/{id}/in-transit")
    public ShipmentResponse markInTransit(@PathVariable UUID id) {
        return mapper.toResponse(
                shipmentUseCases.markInTransit(ShipmentId.of(id))
        );
    }

    /**
     * Marca un envío como disponible para entrega.
     *
     * <p>Indica que el envío ha alcanzado la etapa final de distribución
     * y se encuentra en proceso de entrega al destinatario.</p>
     *
     * @param id identificador único del envío.
     * @return representación actualizada del envío.
     */
    @PostMapping("/{id}/out-for-delivery")
    public ShipmentResponse markOutForDelivery(@PathVariable UUID id) {
        return mapper.toResponse(
                shipmentUseCases.markOutForDelivery(ShipmentId.of(id))
        );
    }

    /**
     * Marca un envío como entregado.
     *
     * <p>Esta operación completa el ciclo normal del envío y lo lleva
     * al estado terminal {@code DELIVERED}.</p>
     *
     * @param id identificador único del envío.
     * @return representación actualizada del envío.
     */
    @PostMapping("/{id}/delivered")
    public ShipmentResponse markDelivered(@PathVariable UUID id) {
        return mapper.toResponse(
                shipmentUseCases.markDelivered(ShipmentId.of(id))
        );
    }

    /**
     * Registra un fallo durante el procesamiento del envío.
     *
     * <p>El caso de uso aplica las reglas de negocio correspondientes
     * para determinar si el envío puede pasar al estado {@code FAILED}.</p>
     *
     * @param id identificador único del envío.
     * @return representación actualizada del envío.
     */
    @PostMapping("/{id}/failed")
    public ShipmentResponse handleFailure(@PathVariable UUID id) {
        return mapper.toResponse(
                shipmentUseCases.handleFailure(ShipmentId.of(id))
        );
    }

    /**
     * Cancela un envío.
     *
     * <p>La operación delega en el caso de uso la validación de las
     * reglas de negocio que determinan si el envío puede ser cancelado
     * según su estado actual.</p>
     *
     * @param id identificador único del envío.
     * @return representación actualizada del envío.
     */
    @PostMapping("/{id}/cancel")
    public ShipmentResponse cancel(@PathVariable UUID id) {
        return mapper.toResponse(
                shipmentUseCases.cancel(ShipmentId.of(id))
        );
    }
}
