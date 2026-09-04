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
     *
     * @param id        identificador de la categoría.
     * @param name      nombre de la categoría.
     * @param parentId  categoría padre (null si es raíz).
     * @param active    si está activa.
     * @param deletedAt fecha de baja (null si no está eliminada).
     * @return la categoría reconstruida.
     * @throws BusinessRuleViolationException si el nombre es inválido.
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
     *
     * @param id       identificador de la categoría.
     * @param name     nombre de la categoría.
     * @param parentId categoría padre (null si es raíz).
     * @return la categoría creada.
     * @throws BusinessRuleViolationException si el nombre es inválido.
     */
    public static Category create(CategoryId id, String name, CategoryId parentId) {
        return crear(id, name, parentId, true, null);
    }

    /**
     * @return la categoría padre, o {@code Optional.empty()} si es raíz.
     */
    public Optional<CategoryId> getParentId() {
        return Optional.ofNullable(parentId);
    }

    /**
     * @return la fecha de baja, o {@code Optional.empty()} si no está eliminada.
     */
    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    /**
     * @return {@code true} si la categoría fue eliminada (soft delete).
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * @param name nuevo nombre de la categoría.
     * @throws BusinessRuleViolationException si el nombre es inválido.
     */
    public void updateName(String name) {
        this.name = requireName(name);
    }

    /**
     * Mueve la categoría bajo otro padre (o a raíz si es {@code null}).
     *
     * @param parentId identificador de la nueva categoría padre.
     * @throws BusinessRuleViolationException si intenta ser su propio padre.
     */
    public void moveTo(CategoryId parentId) {
        if (parentId != null && parentId.equals(id)) {
            throw new BusinessRuleViolationException("Una categoría no puede ser su propio padre");
        }
        this.parentId = parentId;
    }

    /**
     * Activa la categoría.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Desactiva la categoría.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Marca la categoría como eliminada (soft delete).
     *
     * @throws BusinessRuleViolationException si la categoría ya estaba eliminada.
     */
    public void delete() {
        if (isDeleted()) {
            throw new BusinessRuleViolationException("La categoría ya está eliminada: " + id);
        }
        this.deletedAt = Instant.now();
    }

    /**
     * Valida que el nombre sea obligatorio y no exceda 120 caracteres.
     *
     * @param name nombre a validar.
     * @return el nombre si es válido.
     * @throws BusinessRuleViolationException si el nombre está vacío o es demasiado largo.
     */
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