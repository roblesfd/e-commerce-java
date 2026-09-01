package org.fernandodev.ecommerce.shared.domain.exception;

/**
 * Indica la violación de una regla de negocio del dominio.
 * <p>
 * Se usa cuando una operación es inválida por reglas del propio negocio,
 * no por problemas de validación de entrada o de infraestructura.
 * </p>
 */
public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(String message) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }
}