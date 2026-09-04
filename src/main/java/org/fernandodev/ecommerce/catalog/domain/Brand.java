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
     *
     * @param id        identificador de la marca.
     * @param name      nombre de la marca.
     * @param active    si está activa.
     * @param deletedAt fecha de baja (null si no está eliminada).
     * @return la marca reconstruida.
     * @throws BusinessRuleViolationException si el nombre es inválido.
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
     *
     * @param id   identificador de la marca.
     * @param name nombre de la marca.
     * @return la marca creada.
     * @throws BusinessRuleViolationException si el nombre es inválido.
     */
    public static Brand create(BrandId id, String name) {
        return crear(id, name, true, null);
    }

    /**
     * @return la fecha de baja, o {@code Optional.empty()} si no está eliminada.
     */
    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    /**
     * @return {@code true} si la marca fue eliminada (soft delete).
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * @param name nuevo nombre de la marca.
     * @throws BusinessRuleViolationException si el nombre es inválido.
     */
    public void updateName(String name) {
        this.name = requireName(name);
    }

    /**
     * Activa la marca.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Desactiva la marca.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Marca la marca como eliminada (soft delete).
     *
     * @throws BusinessRuleViolationException si la marca ya estaba eliminada.
     */
    public void delete() {
        if (isDeleted()) {
            throw new BusinessRuleViolationException("La marca ya está eliminada: " + id);
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
            throw new BusinessRuleViolationException("El nombre de la marca es obligatorio");
        }
        if (name.length() > 120) {
            throw new BusinessRuleViolationException("El nombre de la marca es demasiado largo (máx. 120 caracteres)");
        }
        return name;
    }
}