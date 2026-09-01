package org.fernandodev.ecommerce.shared.adapter.out.iam;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida que valida los access tokens JWT emitidos por la
 * plataforma IAM.
 * <p>
 * El IAM firma los tokens con {@code HS256} usando un secreto simétrico
 * compartido ({@code iam.jwt.secret}); el claim relevante es {@code sub},
 * que contiene el identificador del usuario IAM. Esta validación es local,
 * sin invocar al IAM por cada request.
 * </p>
 */
@Component
public class JwtAccessTokenValidator {

    private final SecretKey key;

    public JwtAccessTokenValidator(@Value("${iam.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Valida la firma y expiración del token y extrae el identificador del
     * usuario IAM ({@code sub}).
     *
     * @param token token JWT sin prefijo {@code Bearer}.
     * @return el identificador del usuario IAM, o vacío si el token es inválido.
     */
    public Optional<UUID> validateAndExtractSubject(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(UUID.fromString(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}