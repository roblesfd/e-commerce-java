package org.fernandodev.ecommerce.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.application.dto.BrandCommand;
import org.fernandodev.ecommerce.catalog.application.port.out.BrandRepository;
import org.fernandodev.ecommerce.catalog.application.port.out.ProductRepository;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.Barcode;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.Sku;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class BrandServiceTest {

    private final FakeBrandRepository brands = new FakeBrandRepository();
    private final FakeProductRepository products = new FakeProductRepository();
    private final BrandService service = new BrandService(brands, products);

    @Test
    void createGuardaMarca() {
        Brand brand = service.create(new BrandCommand("Nike"));

        assertThat(brand.getId()).isNotNull();
        assertThat(brand.getName()).isEqualTo("Nike");
        assertThat(brand.isActive()).isTrue();
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(BrandId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCambiaNombre() {
        Brand brand = service.create(new BrandCommand("Nike"));

        Brand updated = service.update(brand.getId(), new BrandCommand("Adidas"));

        assertThat(updated.getName()).isEqualTo("Adidas");
    }

    @Test
    void activarYDesactivarCambiaElEstado() {
        Brand brand = service.create(new BrandCommand("Nike"));

        Brand desactivada = service.deactivate(brand.getId());
        assertThat(desactivada.isActive()).isFalse();

        Brand activada = service.activate(brand.getId());
        assertThat(activada.isActive()).isTrue();
    }

    @Test
    void eliminarMarcaConProductosLanzaViolacion() {
        Brand brand = service.create(new BrandCommand("Nike"));
        Product product = Product.create(ProductId.generate(), new Sku("CAMISA"), null, "Camisa", null, brand.getId());
        products.save(product);

        assertThatThrownBy(() -> service.delete(brand.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void eliminarMarcaSinProductosMarcaSoftDelete() {
        Brand brand = service.create(new BrandCommand("Nike"));

        service.delete(brand.getId());

        assertThat(brands.findById(brand.getId()).orElseThrow().isDeleted()).isTrue();
        assertThatThrownBy(() -> service.getById(brand.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listDevuelveSoloNoEliminadas() {
        Brand activa = service.create(new BrandCommand("Nike"));
        Brand borrada = service.create(new BrandCommand("Adidas"));
        service.delete(borrada.getId());

        List<Brand> result = service.list();

        assertThat(result).extracting(Brand::getId).containsExactly(activa.getId());
    }

    private static final class FakeBrandRepository implements BrandRepository {

        private final Map<BrandId, Brand> byId = new HashMap<>();

        @Override
        public Brand save(Brand brand) {
            byId.put(brand.getId(), brand);
            return brand;
        }

        @Override
        public Optional<Brand> findById(BrandId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public boolean existsById(BrandId id) {
            Brand brand = byId.get(id);
            return brand != null && !brand.isDeleted();
        }

        @Override
        public List<Brand> findAll() {
            return byId.values().stream().filter(brand -> !brand.isDeleted()).toList();
        }
    }

    private static final class FakeProductRepository implements ProductRepository {

        private final Map<ProductId, Product> byId = new HashMap<>();

        @Override
        public Product save(Product product) {
            byId.put(product.getId(), product);
            return product;
        }

        @Override
        public Optional<Product> findById(ProductId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public boolean existsBySku(Sku sku) {
            return false;
        }

        @Override
        public boolean existsByBarcode(Barcode barcode) {
            return false;
        }

        @Override
        public boolean existsByBrandId(BrandId brandId) {
            return byId.values().stream().anyMatch(product -> brandId.equals(product.getBrandId()));
        }

        @Override
        public boolean existsByCategoryId(CategoryId categoryId) {
            return false;
        }

        @Override
        public org.fernandodev.ecommerce.catalog.application.dto.PageResult<Product> search(
                org.fernandodev.ecommerce.catalog.application.dto.ProductSearch search,
                org.fernandodev.ecommerce.catalog.application.dto.PageQuery query) {
            return org.fernandodev.ecommerce.catalog.application.dto.PageResult.empty(query.page(), query.size());
        }
    }
}
