package org.fernandodev.ecommerce.checkout.adapter.out.persistence.redis;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.checkout.adapter.util.mapper.CheckoutPersistenceMapper;
import org.fernandodev.ecommerce.checkout.application.port.out.CheckoutRepository;
import org.fernandodev.ecommerce.checkout.domain.Checkout;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adaptador de salida que implementa {@link CheckoutRepository} sobre Redis.
 * <p>
 * Persiste el checkout como JSON bajo las claves {@code checkout:key:{key}} y
 * {@code checkout:id:{id}}. La clave de idempotencia se crea con SETNX para
 * garantizar que un retry resuelva a la misma operación lógica.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class CheckoutRedisRepository implements CheckoutRepository {

    private static final String KEY_BY_IDEMPOTENCY = "checkout:key:";
    private static final String KEY_BY_ID = "checkout:id:";

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    @Value("${checkout.ttl:PT24H}")
    private Duration ttl;

    @Override
    public Optional<Checkout> findByIdempotencyKey(String idempotencyKey) {
        return read(KEY_BY_IDEMPOTENCY + idempotencyKey);
    }

    @Override
    public Optional<Checkout> findById(CheckoutId id) {
        return read(KEY_BY_ID + id.value());
    }

    @Override
    public Optional<Checkout> saveIfAbsent(Checkout checkout) {
        String key = KEY_BY_IDEMPOTENCY + checkout.getIdempotencyKey();
        Boolean created = redis.opsForValue().setIfAbsent(key, serialize(checkout), ttl);
        if (Boolean.TRUE.equals(created)) {
            writeIdKey(checkout);
            return Optional.empty();
        }
        return read(key);
    }

    @Override
    public Checkout save(Checkout checkout) {
        redis.opsForValue().set(KEY_BY_IDEMPOTENCY + checkout.getIdempotencyKey(), serialize(checkout), ttl);
        writeIdKey(checkout);
        return checkout;
    }

    private void writeIdKey(Checkout checkout) {
        redis.opsForValue().set(KEY_BY_ID + checkout.getId().value(), serialize(checkout), ttl);
    }

    private Optional<Checkout> read(String key) {
        String json = redis.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            CheckoutDocument document = jsonMapper.readValue(json, CheckoutDocument.class);
            return Optional.of(CheckoutPersistenceMapper.toDomain(document));
        } catch (Exception e) {
            throw new IllegalStateException("Error al deserializar el checkout desde Redis", e);
        }
    }

    private String serialize(Checkout checkout) {
        try {
            return jsonMapper.writeValueAsString(CheckoutPersistenceMapper.toDocument(checkout));
        } catch (Exception e) {
            throw new IllegalStateException("Error al serializar el checkout en Redis", e);
        }
    }
}