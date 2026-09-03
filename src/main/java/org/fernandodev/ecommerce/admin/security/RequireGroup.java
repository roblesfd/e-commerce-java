package org.fernandodev.ecommerce.admin.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exige que el usuario autenticado pertenezca al grupo indicado (autoridad
 * {@code GRUPO_<valor>}) en el SecurityContext. Verificado por
 * {@link RoleGroupAuthorizationAspect}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireGroup {

    String value();
}