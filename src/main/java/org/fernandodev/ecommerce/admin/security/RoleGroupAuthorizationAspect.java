package org.fernandodev.ecommerce.admin.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Aspecto que verifica las anotaciones {@link RequireRole} y {@link RequireGroup}
 * en el SecurityContext. Si falta la autoridad requerida, lanza
 * {@link AccessDeniedException} (respondido como 403).
 */
@Aspect
@Component
public class RoleGroupAuthorizationAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        requireAuthority("ROLE_" + requireRole.value(),
                "Se requiere el rol " + requireRole.value());
    }

    @Before("@annotation(requireGroup)")
    public void checkGroup(JoinPoint joinPoint, RequireGroup requireGroup) {
        requireAuthority("GRUPO_" + requireGroup.value(),
                "Se requiere pertenecer al grupo " + requireGroup.value());
    }

    private void requireAuthority(String authority, String message) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authorized = authentication != null
                && authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(authority::equals);
        if (!authorized) {
            throw new AccessDeniedException(message);
        }
    }
}