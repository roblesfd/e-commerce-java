package org.fernandodev.ecommerce.shipping.adapter.in.rest;

import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.ShipmentWebhookRequest;
import org.fernandodev.ecommerce.shipping.application.port.in.ShipmentUseCases;
import org.fernandodev.ecommerce.shared.domain.exception.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST encargado de recibir y procesar los webhooks
 * enviados por el proveedor externo de envíos.
 *
 * <p>Los webhooks permiten al proveedor notificar al contexto
 * {@code Shipping} sobre cambios en el estado de un envío, utilizando
 * el número de seguimiento como identificador.</p>
 *
 * <p>La autenticidad de cada solicitud se valida mediante una firma
 * compartida enviada en el header {@code X-Webhook-Signature}. Solo
 * las solicitudes cuya firma coincida con el secreto configurado
 * pueden ser procesadas.</p>
 *
 * <p>El procesamiento del webhook se delega en {@link ShipmentUseCases},
 * donde deben aplicarse las reglas de negocio y la lógica necesaria
 * para garantizar un procesamiento idempotente.</p>
 */
@RestController
@RequestMapping("/api/v1/shipments/webhooks")
@RequiredArgsConstructor
public class ShipmentWebhookController {

    /**
     * Nombre del header HTTP utilizado por el proveedor para enviar
     * la firma que permite validar la autenticidad del webhook.
     */
    private static final String SIGNATURE_HEADER = "X-Webhook-Signature";

    /**
     * Puerto de entrada que expone los casos de uso relacionados
     * con la gestión y actualización de los envíos.
     */
    private final ShipmentUseCases shipmentUseCases;

    /**
     * Secreto compartido utilizado para validar la autenticidad
     * de las solicitudes provenientes del proveedor de envíos.
     *
     * <p>El valor se obtiene mediante la propiedad
     * {@code shipping.webhook-secret}. Si la propiedad no está
     * configurada, se utiliza {@code mock-secret} como valor
     * predeterminado.</p>
     */
    @Value("${shipping.webhook-secret:mock-secret}")
    private String webhookSecret;

    /**
     * Procesa una notificación enviada por el proveedor de envíos.
     *
     * <p>Antes de delegar el procesamiento al caso de uso se valida
     * la firma proporcionada en el header
     * {@code X-Webhook-Signature}. Si la firma no coincide con el
     * secreto configurado, la solicitud es rechazada.</p>
     *
     * <p>Cuando la firma es válida, el número de seguimiento y el nuevo
     * estado recibido se delegan a {@link ShipmentUseCases} para que
     * el dominio determine cómo debe actualizarse el envío.</p>
     *
     * <p>Una vez procesada correctamente la notificación, el endpoint
     * responde con {@link HttpStatus#NO_CONTENT}.</p>
     *
     * @param signature firma proporcionada por el proveedor en el
     *                  header {@code X-Webhook-Signature}.
     * @param request datos de la notificación enviada por el proveedor,
     *                incluyendo el número de seguimiento y el estado
     *                del envío.
     * @throws AuthenticationException si la firma proporcionada no
     *                                 coincide con el secreto configurado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void process(
            @RequestHeader(SIGNATURE_HEADER) String signature,
            @Valid @RequestBody ShipmentWebhookRequest request
    ) {
        if (!webhookSecret.equals(signature)) {
            throw new AuthenticationException("Firma de webhook inválida");
        }

        shipmentUseCases.processWebhook(
                request.trackingNumber(),
                request.status()
        );
    }
}
