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
 * Filtro de autenticación basado en la validación remota de un JWT
 * contra el servicio de IAM.
 *
 * <p>El filtro intercepta cada petición HTTP y busca un token JWT en
 * el encabezado {@code Authorization} utilizando el esquema
 * {@code Bearer}. Cuando encuentra un token, lo envía al
 * {@link TokenValidator} para su validación remota.</p>
 *
 * <p>Si el IAM valida correctamente el token, se construye un
 * {@link AuthenticatedUser} con la identidad del usuario y sus permisos,
 * roles y grupos efectivos. Posteriormente, se crea una autenticación
 * de Spring Security y se establece en el {@link SecurityContextHolder}.</p>
 *
 * <p>Las authorities se construyen utilizando las siguientes convenciones:</p>
 *
 * <ul>
 *     <li>Los permisos se registran directamente como authorities.</li>
 *     <li>Los roles se registran utilizando el prefijo {@code ROLE_}.</li>
 *     <li>Los grupos se registran utilizando el prefijo {@code GRUPO_}.</li>
 * </ul>
 *
 * <p>Si el token es inválido, la validación remota falla o se produce
 * cualquier otra excepción durante el proceso de autenticación, el
 * contexto de seguridad se limpia y la petición continúa sin una
 * autenticación establecida.</p>
 *
 * <p>El filtro extiende {@link OncePerRequestFilter}, garantizando que
 * la lógica de autenticación se ejecute como máximo una vez por petición.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Prefijo utilizado para identificar tokens JWT enviados mediante
     * el esquema de autenticación Bearer.
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Componente encargado de validar remotamente los tokens JWT
     * contra el servicio de IAM.
     */
    private final TokenValidator tokenValidator;

    /**
     * Procesa una petición HTTP y establece la autenticación del usuario
     * cuando existe un JWT válido en el encabezado {@code Authorization}.
     *
     * <p>Si el encabezado contiene un token con el esquema
     * {@code Bearer}, se extrae el token y se delega su validación
     * al método {@link #authenticate(String, HttpServletRequest)}.</p>
     *
     * <p>Independientemente del resultado de la autenticación, la petición
     * continúa su procesamiento mediante el siguiente elemento de la
     * cadena de filtros.</p>
     *
     * @param request petición HTTP actual.
     * @param response respuesta HTTP actual.
     * @param filterChain cadena de filtros que debe continuar procesando
     *                    la petición.
     * @throws ServletException si se produce un error relacionado con
     *                          el procesamiento de la petición.
     * @throws IOException si se produce un error de entrada o salida
     *                     durante el procesamiento de la petición.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            authenticate(
                    header.substring(BEARER_PREFIX.length()),
                    request
            );
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Valida un token JWT y establece la autenticación correspondiente
     * en el contexto de seguridad de Spring Security.
     *
     * <p>Cuando el token es válido, se obtiene del IAM la información
     * necesaria para construir el principal y sus authorities efectivas.</p>
     *
     * <p>Las authorities se generan a partir de los permisos, roles
     * y grupos devueltos por el IAM. Los roles y grupos reciben un
     * prefijo para diferenciarlos de los permisos:</p>
     *
     * <ul>
     *     <li>{@code ROLE_} para roles.</li>
     *     <li>{@code GRUPO_} para grupos.</li>
     * </ul>
     *
     * <p>Si la validación no produce un resultado, no se establece
     * ninguna autenticación. Si se produce una excepción durante
     * el proceso, se limpia explícitamente el contexto de seguridad.</p>
     *
     * @param token JWT extraído del encabezado {@code Authorization}.
     * @param request petición HTTP actual, utilizada para construir
     *                los detalles de autenticación.
     */
    private void authenticate(String token, HttpServletRequest request) {
        try {
            tokenValidator.validate(token).ifPresent(validation -> {

                AuthenticatedUser principal = AuthenticatedUser.of(
                        validation.usuarioId(),
                        validation.username(),
                        validation.permisos(),
                        validation.roles(),
                        validation.grupos()
                );

                Set<String> permisos = validation.permisos() == null
                        ? Set.of()
                        : validation.permisos();

                Set<String> roles = validation.roles() == null
                        ? Set.of()
                        : validation.roles();

                Set<String> grupos = validation.grupos() == null
                        ? Set.of()
                        : validation.grupos();

                List<GrantedAuthority> authorities = new ArrayList<>();

                permisos.forEach(permission ->
                        authorities.add(
                                new SimpleGrantedAuthority(permission)
                        )
                );

                roles.forEach(role ->
                        authorities.add(
                                new SimpleGrantedAuthority("ROLE_" + role)
                        )
                );

                grupos.forEach(group ->
                        authorities.add(
                                new SimpleGrantedAuthority("GRUPO_" + group)
                        )
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            });
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }
    }
}
