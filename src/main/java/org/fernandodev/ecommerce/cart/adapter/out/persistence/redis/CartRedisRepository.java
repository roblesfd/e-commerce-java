package org.fernandodev.ecommerce.cart.adapter.out.persistence.redis;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.cart.adapter.util.mapper.CartPersistenceMapper;
import org.fernandodev.ecommerce.cart.application.port.out.CartRepository;
import org.fernandodev.ecommerce.cart.domain.Cart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adaptador de salida que implementa {@link CartRepository} sobre Redis.
 * <p>
 * Persiste el carrito como JSON bajo la clave {@code cart:{ownerId}} con TTL
 * configurable. El carrito es dato temporal y no autoritativo (spec §25).
 * </p>
 */
@Component
@RequiredArgsConstructor
public class CartRedisRepository implements CartRepository {

    private static final String KEY_PREFIX = "cart:";

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    @Value("${cart.ttl:PT168H}")
    private Duration ttl;

    @Override
    public Cart save(Cart cart) {
        try {
            String json = jsonMapper.writeValueAsString(CartPersistenceMapper.toDocument(cart));
            redis.opsForValue().set(key(cart.getOwnerId()), json, ttl);
        } catch (Exception e) {
            throw new IllegalStateException("Error al serializar el carrito en Redis", e);
        }
        return cart;
    }

    @Override
    public Optional<Cart> findByOwner(UUID ownerId) {
        String json = redis.opsForValue().get(key(ownerId));
        if (json == null) {
            return Optional.empty();
        }
        try {
            CartDocument document = jsonMapper.readValue(json, CartDocument.class);
            return Optional.of(CartPersistenceMapper.toDomain(document));
        } catch (Exception e) {
            throw new IllegalStateException("Error al deserializar el carrito desde Redis", e);
        }
    }

    @Override
    public void delete(UUID ownerId) {
        redis.delete(key(ownerId));
    }

    private static String key(UUID ownerId) {
        return KEY_PREFIX + ownerId;
    }
}