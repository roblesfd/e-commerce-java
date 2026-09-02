package org.fernandodev.ecommerce.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.application.dto.PageQuery;
import org.fernandodev.ecommerce.catalog.application.dto.PageResult;
import org.fernandodev.ecommerce.catalog.application.dto.ProductCommand;
import org.fernandodev.ecommerce.catalog.application.dto.ProductSearch;
import org.fernandodev.ecommerce.catalog.application.port.out.BrandRepository;
import org.fernandodev.ecommerce.catalog.application.port.out.CategoryRepository;
import org.fernandodev.ecommerce.catalog.application.port.out.ProductRepository;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.Barcode;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductAttribute;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.Sku;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class ProductServiceTest {

    private final FakeProductRepository products = new FakeProductRepository();
    private final FakeBrandRepository brands = new FakeBrandRepository();
    private final FakeCategoryRepository categories = new FakeCategoryRepository();
    private final ProductService service = new ProductService(products, brands, categories);

    @Test
    void createGuardaProductoConDetallesCompletos() {
        BrandId marca = brands.save(Brand.create(BrandId.generate(), "Nike")).getId();
        CategoryId categoria = categories.save(Category.create(CategoryId.generate(), "Ropa", null)).getId();

        Product product = service.create(new ProductCommand(
                "CAMISA-NEGRA",
                "7501234567890",
                "Camisa negra",
                "Camisa de algodón",
                marca.value(),
                Set.of(categoria.value()),
                List.of(new ProductCommand.VariantCommand("CAMISA-S", null, List.of(
                        new ProductCommand.AttributeCommand("Talla", "S")))),
                List.of(new ProductCommand.AttributeCommand("Color", "Negro")),
                List.of("https://img.example.com/camisa.jpg")));

        assertThat(product.getSku()).isEqualTo(new Sku("CAMISA-NEGRA"));
        assertThat(product.getBarcode()).isEqualTo(new Barcode("7501234567890"));
        assertThat(product.getBrandId()).isEqualTo(marca);
        assertThat(product.getCategoryIds()).contains(categoria);
        assertThat(product.getVariants()).hasSize(1);
        assertThat(product.getVariants().get(0).sku()).isEqualTo(new Sku("CAMISA-S"));
        assertThat(product.getAttributes()).contains(new ProductAttribute("Color", "Negro"));
        assertThat(product.getImages()).hasSize(1);
        assertThat(products.findById(product.getId())).contains(product);
    }

    @Test
    void createConSkuDuplicadoLanzaConflicto() {
        service.create(comando("CAMISA"));
        assertThatThrownBy(() -> service.create(comando("CAMISA")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createConBarcodeDuplicadoLanzaConflicto() {
        service.create(comando("CAMISA-1", "7500000000001"));
        assertThatThrownBy(() -> service.create(comando("CAMISA-2", "7500000000001")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createConMarcaInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.create(new ProductCommand(
                "CAMISA", null, "Camisa", null, UUID.randomUUID(), Set.of(), List.of(), List.of(), List.of())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createConCategoriaInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.create(new ProductCommand(
                "CAMISA", null, "Camisa", null, null, Set.of(UUID.randomUUID()), List.of(), List.of(), List.of())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(ProductId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateModificaDatosYDetalles() {
        Product original = service.create(comando("CAMISA"));
        BrandId marca = brands.save(Brand.create(BrandId.generate(), "Nike")).getId();

        Product updated = service.update(original.getId(), new ProductCommand(
                "CAMISA-2026",
                null,
                "Camisa 2026",
                "Nueva descripción",
                marca.value(),
                Set.of(),
                List.of(),
                List.of(new ProductCommand.AttributeCommand("Material", "Algodón")),
                List.of()));

        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getSku()).isEqualTo(new Sku("CAMISA-2026"));
        assertThat(updated.getName()).isEqualTo("Camisa 2026");
        assertThat(updated.getBrandId()).isEqualTo(marca);
        assertThat(updated.getVariants()).isEmpty();
        assertThat(updated.getAttributes()).contains(new ProductAttribute("Material", "Algodón"));
    }

    @Test
    void updateConSkuDeOtroProductoLanzaConflicto() {
        service.create(comando("CAMISA-1"));
        Product second = service.create(comando("CAMISA-2"));

        assertThatThrownBy(() -> service.update(second.getId(), comando("CAMISA-1")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void activarYDesactivarCambiaElEstado() {
        Product product = service.create(comando("CAMISA"));

        Product desactivado = service.deactivate(product.getId());
        assertThat(desactivado.isActive()).isFalse();

        Product activado = service.activate(product.getId());
        assertThat(activado.isActive()).isTrue();
    }

    @Test
    void eliminarMarcaSoftDeleteYDejaDeSerVisible() {
        Product product = service.create(comando("CAMISA"));

        service.delete(product.getId());

        assertThat(products.findById(product.getId()).orElseThrow().isDeleted()).isTrue();
        assertThatThrownBy(() -> service.getById(product.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listFiltraPorEstadoYCategoriaYPagina() {
        CategoryId categoria = categories.save(Category.create(CategoryId.generate(), "Ropa", null)).getId();
        Product activo = service.create(new ProductCommand(
                "CAMISA", null, "Camisa", null, null, Set.of(categoria.value()), List.of(), List.of(), List.of()));
        service.create(new ProductCommand(
                "PANTALON", null, "Pantalón", null, null, Set.of(), List.of(), List.of(), List.of()));
        Product desactivado = service.create(new ProductCommand(
                "GORRA", null, "Gorra", null, null, Set.of(), List.of(), List.of(), List.of()));
        service.deactivate(desactivado.getId());

        PageResult<Product> activosEnCategoria = service.list(
                new ProductSearch(true, categoria, null, null), PageQuery.of(0, 10));
        assertThat(activosEnCategoria.content()).extracting(Product::getId)
                .containsExactly(activo.getId());

        PageResult<Product> todos = service.list(ProductSearch.all(), PageQuery.of(0, 2));
        assertThat(todos.totalElements()).isEqualTo(3);
        assertThat(todos.content()).hasSize(2);
        assertThat(todos.totalPages()).isEqualTo(2);
    }

    @Test
    void listExcluyeEliminados() {
        Product product = service.create(comando("CAMISA"));
        service.delete(product.getId());

        PageResult<Product> result = service.list(ProductSearch.all(), PageQuery.of(0, 10));

        assertThat(result.totalElements()).isZero();
    }

    @Test
    void listBuscaPorTexto() {
        service.create(new ProductCommand("CAMISA-NEGRA", null, "Camisa negra", "Camisa", null,
                Set.of(), List.of(), List.of(), List.of()));
        service.create(new ProductCommand("PANTALON-VAQUERO", null, "Pantalón vaquero", "Pantalón", null,
                Set.of(), List.of(), List.of(), List.of()));

        PageResult<Product> result = service.list(new ProductSearch(null, null, null, "camisa"),
                PageQuery.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content().get(0).getName()).isEqualTo("Camisa negra");
    }

    private static ProductCommand comando(String sku) {
        return new ProductCommand(sku, null, "Camisa negra", "Camisa", null,
                Set.of(), List.of(), List.of(), List.of());
    }

    private static ProductCommand comando(String sku, String barcode) {
        return new ProductCommand(sku, barcode, "Camisa negra", "Camisa", null,
                Set.of(), List.of(), List.of(), List.of());
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
        public boolean existsByBarcode(Barcode barcode) {
            return barcode != null && byId.values().stream().anyMatch(product -> barcode.equals(product.getBarcode()));
        }

        @Override
        public boolean existsByBrandId(BrandId brandId) {
            return byId.values().stream().anyMatch(product -> brandId.equals(product.getBrandId()));
        }

        @Override
        public boolean existsByCategoryId(CategoryId categoryId) {
            return byId.values().stream().anyMatch(product -> product.getCategoryIds().contains(categoryId));
        }

        @Override
        public PageResult<Product> search(ProductSearch search, PageQuery query) {
            List<Product> all = byId.values().stream()
                    .filter(product -> !product.isDeleted())
                    .filter(product -> search.active() == null || product.isActive() == search.active())
                    .filter(product -> search.categoryId() == null || product.getCategoryIds().contains(search.categoryId()))
                    .filter(product -> search.brandId() == null || search.brandId().equals(product.getBrandId()))
                    .filter(product -> search.query() == null
                            || product.getName().toLowerCase().contains(search.query().toLowerCase())
                            || product.getSku().value().toLowerCase().contains(search.query().toLowerCase()))
                    .sorted(Comparator.comparing(Product::getName))
                    .toList();
            int from = Math.min(query.page() * query.size(), all.size());
            int to = Math.min(from + query.size(), all.size());
            return PageResult.of(all.subList(from, to), query.page(), query.size(), all.size());
        }
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
}
