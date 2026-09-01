package org.fernandodev.ecommerce.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.shared.adapter.out.iam.JwtAccessTokenValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;

class JwtAuthenticationFilterTest {

    @AfterEach
    void limpiaContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void estableceAutenticacionConTokenValido() throws Exception {
        UUID userId = UUID.randomUUID();
        JwtAccessTokenValidator validator = mock(JwtAccessTokenValidator.class);
        when(validator.validateAndExtractSubject("token-valido")).thenReturn(Optional.of(userId));

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(validator);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getPrincipal()).isEqualTo(new AuthenticatedUser(userId));
    }

    @Test
    void noEstableceAutenticacionSinToken() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mock(JwtAccessTokenValidator.class));
        MockHttpServletRequest request = new MockHttpServletRequest();

        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void noEstableceAutenticacionConTokenInvalido() throws Exception {
        JwtAccessTokenValidator validator = mock(JwtAccessTokenValidator.class);
        when(validator.validateAndExtractSubject("token-invalido")).thenReturn(Optional.empty());

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(validator);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");

        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}