package org.fernandodev.ecommerce.catalog.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa una categoría del catálogo.
 * <p>
 * Soporta jerarquía mediante {@code parentId} (una categoría puede tener
 * subcategorías). La validación de que no tenga hijos ni productos asociados
 * antes de eliminarla se realiza en la capa de aplicación.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Category {

    private CategoryId id;
    private String name;
    private CategoryId parentId;
    private boolean active;
    private Instant deletedAt;

    /**
     * Factoría completa con invariantes. Usada por la persistencia.
     */
    public static Category crear(CategoryId id, String name, CategoryId parentId, boolean active, Instant deletedAt) {
        Category category = new Category();
        category.id = Objects.requireNonNull(id, "id no puede ser null");
        category.name = requireName(name);
        category.parentId = parentId;
        category.active = active;
        category.deletedAt = deletedAt;
        return category;
    }

    /**
     * Crea una categoría activa nueva.
     */
    public static Category create(CategoryId id, String name, CategoryId parentId) {
        return crear(id, name, parentId, true, null);
    }

    public Optional<CategoryId> getParentId() {
        return Optional.ofNullable(parentId);
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

    /**
     * Mueve la categoría bajo otro padre (o a raíz si es {@code null}).
     */
    public void moveTo(CategoryId parentId) {
        if (parentId != null && parentId.equals(id)) {
            throw new BusinessRuleViolationException("Una categoría no puede ser su propio padre");
        }
        this.parentId = parentId;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    /**
     * Marca la categoría como eliminada (soft delete).
     */
    public void delete() {
        if (isDeleted()) {
            throw new BusinessRuleViolationException("La categoría ya está eliminada: " + id);
        }
        this.deletedAt = Instant.now();
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("El nombre de la categoría es obligatorio");
        }
        if (name.length() > 120) {
            throw new BusinessRuleViolationException("El nombre de la categoría es demasiado largo (máx. 120 caracteres)");
        }
        return name;
    }
}