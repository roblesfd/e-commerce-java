package org.fernandodev.ecommerce.shared.infrastructure.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fernandodev.starter.iam.client.TokenValidator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filtro de autenticación por validación remota del JWT contra el IAM.
 * <p>
 * Lee el token del encabezado {@code Authorization: Bearer}, lo valida
 * remotamente ({@link TokenValidator} → {@code POST /auth/validar}) y
 * construye un {@link AuthenticatedUser} con la identidad y los permisos
 * efectivos que devuelve el IAM. Si el token es inválido o el IAM no está
 * disponible, el contexto permanece vacío.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenValidator tokenValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            authenticate(header.substring(BEARER_PREFIX.length()), request);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        try {
            tokenValidator.validate(token).ifPresent(validation -> {
                AuthenticatedUser principal = AuthenticatedUser.of(
                        validation.usuarioId(), validation.username(),
                        validation.permisos(), validation.roles(), validation.grupos());

                List<GrantedAuthority> authorities = new ArrayList<>();
                Set<String> permisos = validation.permisos() == null ? Set.of() : validation.permisos();
                Set<String> roles = validation.roles() == null ? Set.of() : validation.roles();
                Set<String> grupos = validation.grupos() == null ? Set.of() : validation.grupos();

                permisos.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
                roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
                grupos.forEach(g -> authorities.add(new SimpleGrantedAuthority("GRUPO_" + g)));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }
    }
}