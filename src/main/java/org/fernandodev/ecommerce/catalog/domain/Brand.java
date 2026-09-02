package org.fernandodev.ecommerce.catalog.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa una marca del catálogo.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Brand {

    private BrandId id;
    private String name;
    private boolean active;
    private Instant deletedAt;

    /**
     * Factoría completa con invariantes. Usada por la persistencia.
     */
    public static Brand crear(BrandId id, String name, boolean active, Instant deletedAt) {
        Brand brand = new Brand();
        brand.id = Objects.requireNonNull(id, "id no puede ser null");
        brand.name = requireName(name);
        brand.active = active;
        brand.deletedAt = deletedAt;
        return brand;
    }

    /**
     * Crea una marca activa nueva.
     */
    public static Brand create(BrandId id, String name) {
        return crear(id, name, true, null);
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void updateName(String name) {
        this.name = requireName(name);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    /**
     * Marca la marca como eliminada (soft delete).
     */
    public void delete() {
        if (isDeleted()) {
            throw new BusinessRuleViolationException("La marca ya está eliminada: " + id);
        }
        this.deletedAt = Instant.now();
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("El nombre de la marca es obligatorio");
        }
        if (name.length() > 120) {
            throw new BusinessRuleViolationException("El nombre de la marca es demasiado largo (máx. 120 caracteres)");
        }
        return name;
    }
}