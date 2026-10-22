package org.fernandodev.ecommerce.returns.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.CreateReturnRequest;
import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.InspectReturnRequest;
import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.RegisterReturnShipmentRequest;
import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.ReturnResponse;
import org.fernandodev.ecommerce.returns.adapter.in.rest.mapper.ReturnMapper;
import org.fernandodev.ecommerce.returns.application.port.in.ReturnUseCases;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
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
 * Controlador REST para la gestión de solicitudes de devolución.
 *
 * <p>Expone los endpoints necesarios para crear, consultar y gestionar
 * el ciclo de vida de una devolución dentro del contexto Returns.</p>
 *
 * <p>El controlador se limita a gestionar aspectos HTTP y delega la
 * lógica de negocio en {@link ReturnUseCases}. La transformación entre
 * modelos HTTP y modelos de aplicación se realiza mediante
 * {@link ReturnMapper}.</p>
 */
@RestController
@RequestMapping("/api/v1/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnUseCases returnUseCases;
    private final ReturnMapper mapper;

    /**
     * Crea una nueva solicitud de devolución para una orden.
     *
     * @param request datos necesarios para crear la solicitud de devolución.
     * @return la devolución creada.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnResponse create(@Valid @RequestBody CreateReturnRequest request) {
        return mapper.toResponse(returnUseCases.create(
                OrderId.of(request.orderId()), mapper.toCommand(request)));
    }

    /**
     * Obtiene una solicitud de devolución por su identificador.
     *
     * @param id identificador único de la devolución.
     * @return la devolución solicitada.
     */
    @GetMapping("/{id}")
    public ReturnResponse get(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.getById(ReturnId.of(id)));
    }

    /**
     * Obtiene las solicitudes de devolución pertenecientes al cliente
     * autenticado.
     *
     * @return lista de devoluciones del cliente autenticado.
     */
    @GetMapping("/me")
    public List<ReturnResponse> mine() {
        return returnUseCases.listByCustomer(SecurityUtils.requiredUserId()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Obtiene la solicitud de devolución asociada a una orden.
     *
     * @param orderId identificador único de la orden.
     * @return la devolución asociada a la orden.
     * @throws ResourceNotFoundException si no existe una devolución
     * asociada a la orden indicada.
     */
    @GetMapping
    public ReturnResponse getByOrder(@RequestParam UUID orderId) {
        return returnUseCases.getByOrder(OrderId.of(orderId))
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Devolución no encontrada para la orden: " + orderId));
    }

    /**
     * Aprueba una solicitud de devolución.
     *
     * @param id identificador único de la devolución.
     * @return la devolución con su estado actualizado.
     */
    @PostMapping("/{id}/approve")
    public ReturnResponse approve(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.approve(ReturnId.of(id)));
    }

    /**
     * Rechaza una solicitud de devolución.
     *
     * @param id identificador único de la devolución.
     * @return la devolución con su estado actualizado.
     */
    @PostMapping("/{id}/reject")
    public ReturnResponse reject(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.reject(ReturnId.of(id)));
    }

    /**
     * Cancela una solicitud de devolución.
     *
     * @param id identificador único de la devolución.
     * @return la devolución con su estado actualizado.
     */
    @PostMapping("/{id}/cancel")
    public ReturnResponse cancel(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.cancel(ReturnId.of(id)));
    }

    /**
     * Registra el envío de un producto devuelto y su número de seguimiento.
     *
     * @param id identificador único de la devolución.
     * @param request datos del envío de devolución.
     * @return la devolución con la información del envío registrada.
     */
    @PostMapping("/{id}/return-shipment")
    public ReturnResponse registerReturnShipment(
            @PathVariable UUID id,
            @Valid @RequestBody RegisterReturnShipmentRequest request) {

        return mapper.toResponse(
                returnUseCases.registerReturnShipment(
                        ReturnId.of(id),
                        request.trackingNumber()));
    }

    /**
     * Registra la recepción del producto devuelto.
     *
     * @param id identificador único de la devolución.
     * @return la devolución con su estado actualizado.
     */
    @PostMapping("/{id}/receive")
    public ReturnResponse receive(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.receive(ReturnId.of(id)));
    }

    /**
     * Registra el resultado de la inspección del producto devuelto.
     *
     * @param id identificador único de la devolución.
     * @param request datos y resultado de la inspección.
     * @return la devolución con la información de inspección registrada.
     */
    @PostMapping("/{id}/inspect")
    public ReturnResponse inspect(
            @PathVariable UUID id,
            @Valid @RequestBody InspectReturnRequest request) {

        return mapper.toResponse(
                returnUseCases.inspect(
                        ReturnId.of(id),
                        request.condition(),
                        request.notes(),
                        request.passed()));
    }

    /**
     * Completa el proceso de devolución.
     *
     * @param id identificador único de la devolución.
     * @return la devolución con su estado actualizado.
     */
    @PostMapping("/{id}/complete")
    public ReturnResponse complete(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.complete(ReturnId.of(id)));
    }

    /**
     * Reintenta el procesamiento del reembolso asociado a una devolución.
     *
     * <p>Esta operación se utiliza cuando el procesamiento anterior
     * del reembolso ha fallado.</p>
     *
     * @param id identificador único de la devolución.
     * @return la devolución con el estado actualizado del reembolso.
     */
    @PostMapping("/{id}/retry-refund")
    public ReturnResponse retryRefund(@PathVariable UUID id) {
        return mapper.toResponse(returnUseCases.retryRefund(ReturnId.of(id)));
    }
}
