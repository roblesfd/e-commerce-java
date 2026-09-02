package org.fernandodev.ecommerce.catalog.adapter.in.rest.mapper;

import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.BrandRequest;
import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.BrandResponse;
import org.fernandodev.ecommerce.catalog.application.dto.BrandCommand;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre DTOs REST y objetos de aplicación/dominio de marcas.
 */
@Component
public class BrandMapper {

    public BrandCommand toCommand(BrandRequest request) {
        return new BrandCommand(request.name());
    }

    public BrandResponse toResponse(Brand brand) {
        return new BrandResponse(brand.getId().value(), brand.getName(), brand.isActive());
    }
}