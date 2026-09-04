package org.fernandodev.ecommerce.catalog.application.service;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.application.dto.CategoryCommand;
import org.fernandodev.ecommerce.catalog.application.port.in.CategoryUseCases;
import org.fernandodev.ecommerce.catalog.application.port.out.CategoryRepository;
import org.fernandodev.ecommerce.catalog.application.port.out.ProductRepository;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de categorías del contexto Product Catalog.
 */
@RequiredArgsConstructor
public class CategoryService implements CategoryUseCases {

    private final CategoryRepository categories;
    private final ProductRepository products;

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException si la categoría padre no existe.
     * @throws BusinessRuleViolationException si intenta ser su propio padre.
     */
    @Override
    public Category create(CategoryCommand command) {
        CategoryId parentId = requireParent(command.parentId(), null);
        return categories.save(Category.create(CategoryId.generate(), command.name(), parentId));
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException si la categoría no existe o está eliminada.
     */
    @Override
    public Category getById(CategoryId id) {
        return getExisting(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Category> list() {
        return categories.findAll();
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException     si la categoría o su padre no existen.
     * @throws BusinessRuleViolationException si intenta ser su propio padre.
     */
    @Override
    public Category update(CategoryId id, CategoryCommand command) {
        Category category = getExisting(id);
        CategoryId parentId = requireParent(command.parentId(), id);
        category.updateName(command.name());
        category.moveTo(parentId);
        return categories.save(category);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException si la categoría no existe o está eliminada.
     */
    @Override
    public Category activate(CategoryId id) {
        Category category = getExisting(id);
        category.activate();
        return categories.save(category);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException si la categoría no existe o está eliminada.
     */
    @Override
    public Category deactivate(CategoryId id) {
        Category category = getExisting(id);
        category.deactivate();
        return categories.save(category);
    }

    /**
     * {@inheritDoc}
     *
     * @throws BusinessRuleViolationException si la categoría tiene subcategorías o productos asociados.
     * @throws ResourceNotFoundException      si la categoría no existe o está eliminada.
     */
    @Override
    public void delete(CategoryId id) {
        Category category = getExisting(id);
        if (categories.existsByParentId(id)) {
            throw new BusinessRuleViolationException("No se puede eliminar una categoría con subcategorías");
        }
        if (products.existsByCategoryId(id)) {
            throw new BusinessRuleViolationException("No se puede eliminar una categoría con productos asociados");
        }
        category.delete();
        categories.save(category);
    }

    /**
     * @param id identificador de la categoría.
     * @return la categoría no eliminada.
     * @throws ResourceNotFoundException si no existe o está eliminada.
     */
    private Category getExisting(CategoryId id) {
        Category category = categories.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + id));
        if (category.isDeleted()) {
            throw new ResourceNotFoundException("Categoría no encontrada: " + id);
        }
        return category;
    }

    /**
     * Valida el padre de una categoría (existente, no eliminado y distinto de sí misma).
     *
     * @param parentId  identificador del padre (opcional).
     * @param currentId categoría que se está creando/actualizando.
     * @return el identificador validado, o {@code null} si no se indicó.
     * @throws BusinessRuleViolationException si intenta ser su propio padre.
     * @throws ResourceNotFoundException      si el padre no existe o está eliminado.
     */
    private CategoryId requireParent(UUID parentId, CategoryId currentId) {
        if (parentId == null) {
            return null;
        }
        CategoryId parent = CategoryId.of(parentId);
        if (parent.equals(currentId)) {
            throw new BusinessRuleViolationException("Una categoría no puede ser su propio padre");
        }
        Category existing = categories.findById(parent)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría padre no encontrada: " + parent));
        if (existing.isDeleted()) {
            throw new ResourceNotFoundException("Categoría padre no encontrada: " + parent);
        }
        return parent;
    }
}
