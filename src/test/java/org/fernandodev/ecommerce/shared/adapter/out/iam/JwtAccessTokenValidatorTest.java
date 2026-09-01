package org.fernandodev.ecommerce.shared.adapter.out.iam;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtAccessTokenValidatorTest {

    private static final String SECRET = "changemechangemechangemechangeme";

    @Test
    void validaTokenFirmadoConElMismoSecret() {
        UUID userId = UUID.randomUUID();
        String token = firmarConSecret(userId, SECRET, Duration.ofHours(1));

        var validator = new JwtAccessTokenValidator(SECRET);

        assertThat(validator.validateAndExtractSubject(token)).contains(userId);
    }

    @Test
    void rechazaTokenConFirmaDistinta() {
        UUID userId = UUID.randomUUID();
        String token = firmarConSecret(userId, "otro-secreto-otro-secreto-otro-secreto", Duration.ofHours(1));

        var validator = new JwtAccessTokenValidator(SECRET);

        assertThat(validator.validateAndExtractSubject(token)).isEmpty();
    }

    @Test
    void rechazaTokenVencido() {
        UUID userId = UUID.randomUUID();
        Instant ahora = Instant.now();
        String token = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(ahora.minusSeconds(3600)))
                .expiration(Date.from(ahora.minusSeconds(1)))
                .signWith(clave(SECRET), Jwts.SIG.HS256)
                .compact();

        var validator = new JwtAccessTokenValidator(SECRET);

        assertThat(validator.validateAndExtractSubject(token)).isEmpty();
    }

    @Test
    void rechazaTokenMalformado() {
        var validator = new JwtAccessTokenValidator(SECRET);

        assertThat(validator.validateAndExtractSubject("no-es-un-jwt")).isEmpty();
    }

    private static String firmarConSecret(UUID userId, String secret, Duration expiracion) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracion)))
                .signWith(clave(secret), Jwts.SIG.HS256)
                .compact();
    }

    private static SecretKey clave(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}