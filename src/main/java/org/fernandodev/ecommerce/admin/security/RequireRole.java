package org.fernandodev.ecommerce.admin.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exige que el usuario autenticado tenga el rol indicado (autoridad
 * {@code ROLE_<valor>}) en el SecurityContext. Verificado por
 * {@link RoleGroupAuthorizationAspect}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    String value();
}