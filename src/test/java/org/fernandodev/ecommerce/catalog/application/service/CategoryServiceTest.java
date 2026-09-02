package org.fernandodev.ecommerce.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.application.dto.CategoryCommand;
import org.fernandodev.ecommerce.catalog.application.port.out.CategoryRepository;
import org.fernandodev.ecommerce.catalog.application.port.out.ProductRepository;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.Sku;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class CategoryServiceTest {

    private final FakeCategoryRepository categories = new FakeCategoryRepository();
    private final FakeProductRepository products = new FakeProductRepository();
    private final CategoryService service = new CategoryService(categories, products);

    @Test
    void createGuardaCategoria() {
        Category category = service.create(new CategoryCommand("Electrónica", null));

        assertThat(category.getId()).isNotNull();
        assertThat(category.getName()).isEqualTo("Electrónica");
        assertThat(category.isActive()).isTrue();
    }

    @Test
    void createConPadreValidoEstableceJerarquia() {
        CategoryId padre = categories.save(Category.create(CategoryId.generate(), "Electrónica", null)).getId();

        Category hija = service.create(new CategoryCommand("Laptops", padre.value()));

        assertThat(hija.getParentId()).contains(padre);
    }

    @Test
    void createConPadreInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.create(new CategoryCommand("Laptops", java.util.UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(CategoryId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCambiaNombreYPadre() {
        Category category = service.create(new CategoryCommand("Electrónica", null));
        CategoryId nuevoPadre = categories.save(Category.create(CategoryId.generate(), "Tienda", null)).getId();

        Category updated = service.update(category.getId(), new CategoryCommand("Tecnología", nuevoPadre.value()));

        assertThat(updated.getName()).isEqualTo("Tecnología");
        assertThat(updated.getParentId()).contains(nuevoPadre);
    }

    @Test
    void updateConPadreIgualAlPropioIdLanzaViolacion() {
        Category category = service.create(new CategoryCommand("Electrónica", null));

        assertThatThrownBy(() -> service.update(category.getId(),
                new CategoryCommand("Electrónica", category.getId().value())))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void eliminarCategoriaConSubcategoriasLanzaViolacion() {
        Category padre = service.create(new CategoryCommand("Electrónica", null));
        service.create(new CategoryCommand("Laptops", padre.getId().value()));

        assertThatThrownBy(() -> service.delete(padre.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void eliminarCategoriaConProductosLanzaViolacion() {
        Category category = service.create(new CategoryCommand("Ropa", null));
        Product product = Product.create(ProductId.generate(), new Sku("CAMISA"), null, "Camisa", null, null);
        product.associateCategory(category.getId());
        products.save(product);

        assertThatThrownBy(() -> service.delete(category.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void eliminarCategoriaSinDependenciasMarcaSoftDelete() {
        Category category = service.create(new CategoryCommand("Electrónica", null));

        service.delete(category.getId());

        assertThat(categories.findById(category.getId()).orElseThrow().isDeleted()).isTrue();
        assertThatThrownBy(() -> service.getById(category.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void activarYDesactivarCambiaElEstado() {
        Category category = service.create(new CategoryCommand("Electrónica", null));

        Category desactivada = service.deactivate(category.getId());
        assertThat(desactivada.isActive()).isFalse();

        Category activada = service.activate(category.getId());
        assertThat(activada.isActive()).isTrue();
    }

    @Test
    void listDevuelveSoloNoEliminadas() {
        Category activa = service.create(new CategoryCommand("Electrónica", null));
        Category borrada = service.create(new CategoryCommand("Ropa", null));
        service.delete(borrada.getId());

        List<Category> result = service.list();

        assertThat(result).extracting(Category::getId).containsExactly(activa.getId());
    }

    private static final class FakeCategoryRepository implements CategoryRepository {

        private final Map<CategoryId, Category> byId = new HashMap<>();

        @Override
        public Category save(Category category) {
            byId.put(category.getId(), category);
            return category;
        }

        @Override
        public Optional<Category> findById(CategoryId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public boolean existsById(CategoryId id) {
            Category category = byId.get(id);
            return category != null && !category.isDeleted();
        }

        @Override
        public boolean existsByParentId(CategoryId parentId) {
            return byId.values().stream()
                    .anyMatch(category -> !category.isDeleted()
                            && category.getParentId().filter(p -> p.equals(parentId)).isPresent());
        }

        @Override
        public List<Category> findAll() {
            return byId.values().stream().filter(category -> !category.isDeleted()).toList();
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
            return byId.values().stream().anyMatch(product -> sku.equals(product.getSku()));
        }

        @Override
        public boolean existsByBarcode(org.fernandodev.ecommerce.catalog.domain.value_object.Barcode barcode) {
            return false;
        }

        @Override
        public boolean existsByBrandId(org.fernandodev.ecommerce.catalog.domain.value_object.BrandId brandId) {
            return false;
        }

        @Override
        public boolean existsByCategoryId(CategoryId categoryId) {
            return byId.values().stream().anyMatch(product -> product.getCategoryIds().contains(categoryId));
        }

        @Override
        public org.fernandodev.ecommerce.catalog.application.dto.PageResult<Product> search(
                org.fernandodev.ecommerce.catalog.application.dto.ProductSearch search,
                org.fernandodev.ecommerce.catalog.application.dto.PageQuery query) {
            return org.fernandodev.ecommerce.catalog.application.dto.PageResult.empty(query.page(), query.size());
        }
    }
}
