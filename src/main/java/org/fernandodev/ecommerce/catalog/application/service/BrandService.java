package org.fernandodev.ecommerce.catalog.application.service;

import java.util.List;

import org.fernandodev.ecommerce.catalog.application.dto.BrandCommand;
import org.fernandodev.ecommerce.catalog.application.port.in.BrandUseCases;
import org.fernandodev.ecommerce.catalog.application.port.out.BrandRepository;
import org.fernandodev.ecommerce.catalog.application.port.out.ProductRepository;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de marcas del contexto Product Catalog.
 */
@RequiredArgsConstructor
public class BrandService implements BrandUseCases {

    private final BrandRepository brands;
    private final ProductRepository products;

    @Override
    public Brand create(BrandCommand command) {
        return brands.save(Brand.create(BrandId.generate(), command.name()));
    }

    @Override
    public Brand getById(BrandId id) {
        return getExisting(id);
    }

    @Override
    public List<Brand> list() {
        return brands.findAll();
    }

    @Override
    public Brand update(BrandId id, BrandCommand command) {
        Brand brand = getExisting(id);
        brand.updateName(command.name());
        return brands.save(brand);
    }

    @Override
    public Brand activate(BrandId id) {
        Brand brand = getExisting(id);
        brand.activate();
        return brands.save(brand);
    }

    @Override
    public Brand deactivate(BrandId id) {
        Brand brand = getExisting(id);
        brand.deactivate();
        return brands.save(brand);
    }

    @Override
    public void delete(BrandId id) {
        Brand brand = getExisting(id);
        if (products.existsByBrandId(id)) {
            throw new BusinessRuleViolationException("No se puede eliminar una marca con productos asociados");
        }
        brand.delete();
        brands.save(brand);
    }

    private Brand getExisting(BrandId id) {
        Brand brand = brands.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada: " + id));
        if (brand.isDeleted()) {
            throw new ResourceNotFoundException("Marca no encontrada: " + id);
        }
        return brand;
    }
}
