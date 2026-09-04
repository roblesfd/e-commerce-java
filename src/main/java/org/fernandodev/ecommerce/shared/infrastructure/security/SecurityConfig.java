package org.fernandodev.ecommerce.shared.infrastructure.security;

import tools.jackson.databind.json.JsonMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.fernandodev.ecommerce.shared.adapter.in.rest.error.ApiError;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
/**
 * Configuración principal de Spring Security para la aplicación.
 *
 * <p>Define una arquitectura de seguridad stateless en la que la
 * autenticación es delegada al IAM mediante {@link JwtAuthenticationFilter}.
 * El filtro valida remotamente los tokens JWT y establece el usuario
 * autenticado en el contexto de seguridad.</p>
 *
 * <p>La autorización de la aplicación no replica la administración de
 * roles y permisos del IAM. El acceso a los recursos se determina
 * principalmente mediante la identidad del usuario autenticado y las
 * reglas de autorización definidas en la capa correspondiente.</p>
 *
 * <p>La configuración también establece el tratamiento de errores de
 * autenticación y autorización, devolviendo respuestas JSON mediante
 * {@link ApiError}.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Filtro encargado de extraer y validar remotamente los JWT
     * presentes en las peticiones HTTP.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Componente encargado de serializar las respuestas de error
     * en formato JSON.
     */
    private final JsonMapper jsonMapper;

    /**
     * Configura la cadena de filtros y las reglas principales de
     * seguridad HTTP de la aplicación.
     *
     * <p>La configuración establece las siguientes políticas:</p>
     *
     * <ul>
     *     <li>Deshabilita CSRF, dado que la aplicación utiliza una
     *         arquitectura stateless basada en tokens.</li>
     *     <li>Deshabilita la creación y utilización de sesiones HTTP
     *         mediante {@link SessionCreationPolicy#STATELESS}.</li>
     *     <li>Permite el acceso público a la documentación OpenAPI,
     *         Swagger UI, webhooks específicos y el endpoint de health.</li>
     *     <li>Requiere autenticación para cualquier otro endpoint.</li>
     *     <li>Registra {@link JwtAuthenticationFilter} antes del filtro
     *         estándar de autenticación mediante username y password.</li>
     * </ul>
     *
     * <p>Los errores de autenticación y autorización se transforman
     * respectivamente en respuestas HTTP {@code 401 Unauthorized} y
     * {@code 403 Forbidden} con un cuerpo JSON.</p>
     *
     * @param http objeto utilizado para configurar la seguridad HTTP.
     * @return cadena de filtros de seguridad configurada.
     * @throws Exception si se produce un error durante la construcción
     *                   de la configuración de seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) ->
                                writeError(
                                        request,
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "No autenticado"
                                )
                        )
                        .accessDeniedHandler((request, response, ex) ->
                                writeError(
                                        request,
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "Acceso denegado"
                                )
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/payments/webhooks",
                                "/api/v1/shipments/webhooks"
                        ).permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * Construye y escribe una respuesta de error HTTP en formato JSON.
     *
     * <p>El cuerpo de la respuesta se construye mediante
     * {@link ApiError} e incluye información contextual de la petición,
     * así como el identificador de correlación disponible en el
     * {@link MDC}.</p>
     *
     * @param request petición HTTP que originó el error.
     * @param response respuesta HTTP que será enviada al cliente.
     * @param status estado HTTP que debe devolverse.
     * @param message mensaje descriptivo asociado al error.
     * @throws IOException si ocurre un error al escribir la respuesta.
     */
    private void writeError(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message) throws IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiError body = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                MDC.get("correlationId")
        );

        jsonMapper.writeValue(response.getWriter(), body);
    }
}
