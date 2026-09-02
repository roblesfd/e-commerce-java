package org.fernandodev.ecommerce.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.starter.iam.client.TokenValidator;
import org.fernandodev.starter.iam.dto.ValidarTokenResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;

class JwtAuthenticationFilterTest {

    @AfterEach
    void limpiaContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void estableceAutenticacionConTokenValido() throws Exception {
        UUID userId = UUID.randomUUID();
        TokenValidator validator = mock(TokenValidator.class);
        when(validator.validate(anyString())).thenReturn(Optional.of(
                new ValidarTokenResponse(userId, "ana", true, true, Set.of("USER_READ"))));

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(validator);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        assertThat(principal.id()).isEqualTo(userId);
        assertThat(principal.permisos()).contains("USER_READ");
    }

    @Test
    void noEstableceAutenticacionSinToken() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mock(TokenValidator.class));
        MockHttpServletRequest request = new MockHttpServletRequest();

        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void noEstableceAutenticacionConTokenInvalido() throws Exception {
        TokenValidator validator = mock(TokenValidator.class);
        when(validator.validate(anyString())).thenReturn(Optional.empty());

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(validator);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");

        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void limpiaContextoSiElIamNoResponde() throws Exception {
        TokenValidator validator = mock(TokenValidator.class);
        when(validator.validate(anyString())).thenThrow(new RuntimeException("IAM caído"));

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(validator);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");

        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}