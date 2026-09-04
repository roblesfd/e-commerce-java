package org.fernandodev.ecommerce.catalog.adapter.in.rest.mapper;

import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.CategoryRequest;
import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.CategoryResponse;
import org.fernandodev.ecommerce.catalog.application.dto.CategoryCommand;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre DTOs REST y objetos de aplicación/dominio de categorías.
 */
@Component
public class CategoryMapper {

    /**
     * @param request petición de categoría.
     * @return comando de aplicación equivalente.
     */
    public CategoryCommand toCommand(CategoryRequest request) {
        return new CategoryCommand(request.name(), request.parentId());
    }

    /**
     * @param category agregado de dominio.
     * @return respuesta REST con la categoría.
     */
    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId().value(),
                category.getName(),
                category.getParentId().map(parent -> parent.value()).orElse(null),
                category.isActive());
    }
}