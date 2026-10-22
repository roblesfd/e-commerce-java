package org.fernandodev.ecommerce.returns.domain.value_object;

/**
 * Motivo por el cual un cliente solicita una devolución.
 *
 * <p>Representa la causa declarada para la devolución de un producto.
 * Este valor es utilizado por el contexto Returns para registrar,
 * clasificar y gestionar las solicitudes de devolución.</p>
 */
public enum ReturnReason {

    /**
     * El producto presenta un defecto o no funciona correctamente.
     */
    DEFECTIVE,

    /**
     * El cliente recibió un producto diferente al solicitado.
     */
    WRONG_ITEM,

    /**
     * El producto recibido no corresponde con la descripción
     * o características publicadas.
     */
    NOT_AS_DESCRIBED,

    /**
     * El cliente ya no desea conservar el producto,
     * aunque este no presente necesariamente un defecto.
     */
    NO_LONGER_WANTED,

    /**
     * La razón de la devolución no corresponde con ninguna
     * de las categorías definidas.
     */
    OTHER
}
