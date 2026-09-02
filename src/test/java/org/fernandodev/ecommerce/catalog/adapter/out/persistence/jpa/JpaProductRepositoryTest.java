package org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.application.dto.PageQuery;
import org.fernandodev.ecommerce.catalog.application.dto.PageResult;
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
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductImage;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariant;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.catalog.domain.value_object.Sku;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaProductRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private BrandRepository brands;

    @Autowired
    private CategoryRepository categories;

    @Test
    void guardaYRecuperaProductoConDetallesCompletos() {
        BrandId marca = brands.save(Brand.create(BrandId.generate(), "Nike")).getId();
        CategoryId categoria = categories.save(Category.create(CategoryId.generate(), "Ropa", null)).getId();
        Product product = producto("CAMISA", "7500000000001", marca, categoria);

        repository.save(product);
        Product loaded = repository.findById(product.getId()).orElseThrow();

        assertThat(loaded.getSku()).isEqualTo(product.getSku());
        assertThat(loaded.getBarcode()).isEqualTo(new Barcode("7500000000001"));
        assertThat(loaded.getBrandId()).isEqualTo(marca);
        assertThat(loaded.getCategoryIds()).containsExactly(categoria);
        assertThat(loaded.getAttributes()).contains(new ProductAttribute("Color", "Negro"));
        assertThat(loaded.getImages()).containsExactly(new ProductImage("https://img.example.com/a.jpg"));
        assertThat(loaded.getVariants()).hasSize(1);
        ProductVariant variant = loaded.getVariants().get(0);
        assertThat(variant.sku()).isEqualTo(new Sku("CAMISA-S"));
        assertThat(variant.attributes()).contains(new ProductAttribute("Talla", "S"));
    }

    @Test
    void existsBySkuYBarcodeTrasGuardar() {
        Product product = producto("CAMISA", "7500000000002", null, null);

        repository.save(product);

        assertThat(repository.existsBySku(new Sku("CAMISA"))).isTrue();
        assertThat(repository.existsBySku(new Sku("OTRO"))).isFalse();
        assertThat(repository.existsByBarcode(new Barcode("7500000000002"))).isTrue();
    }

    @Test
    void existsByMarcaYCategoriaTrasGuardar() {
        BrandId marca = brands.save(Brand.create(BrandId.generate(), "Nike")).getId();
        CategoryId categoria = categories.save(Category.create(CategoryId.generate(), "Ropa", null)).getId();
        repository.save(producto("CAMISA", null, marca, categoria));

        assertThat(repository.existsByBrandId(marca)).isTrue();
        assertThat(repository.existsByCategoryId(categoria)).isTrue();
        assertThat(repository.existsByBrandId(BrandId.generate())).isFalse();
    }

    @Test
    void searchFiltraPorEstadoYCategoria() {
        CategoryId categoria = categories.save(Category.create(CategoryId.generate(), "Ropa", null)).getId();
        Product activo = repository.save(producto("CAMISA", null, null, categoria));
        repository.save(producto("PANTALON", null, null, null));
        Product desactivado = repository.save(producto("GORRA", null, null, null));
        desactivado.deactivate();
        repository.save(desactivado);

        PageResult<Product> activosEnCategoria = repository.search(
                new ProductSearch(true, categoria, null, null), PageQuery.of(0, 10));

        assertThat(activosEnCategoria.totalElements()).isEqualTo(1);
        assertThat(activosEnCategoria.content().get(0).getId()).isEqualTo(activo.getId());
    }

    @Test
    void searchPaginaResultadosOrdenadosPorNombre() {
        repository.save(producto("CAMISA", null, null, null));
        repository.save(producto("PANTALON", null, null, null));
        repository.save(producto("GORRA", null, null, null));

        PageResult<Product> page1 = repository.search(ProductSearch.all(), PageQuery.of(0, 2));
        PageResult<Product> page2 = repository.search(ProductSearch.all(), new PageQuery(1, 2, "name", "asc"));

        assertThat(page1.totalElements()).isEqualTo(3);
        assertThat(page1.content()).extracting(p -> p.getSku().value())
                .containsExactly("CAMISA", "GORRA");
        assertThat(page2.content()).extracting(p -> p.getSku().value())
                .containsExactly("PANTALON");
    }

    @Test
    void searchExcluyeEliminados() {
        Product product = repository.save(producto("CAMISA", null, null, null));
        product.delete();
        repository.save(product);

        PageResult<Product> result = repository.search(ProductSearch.all(), PageQuery.of(0, 10));

        assertThat(result.totalElements()).isZero();
    }

    @Test
    void searchBuscaPorTexto() {
        repository.save(producto("CAMISA", null, null, null));

        PageResult<Product> result = repository.search(
                new ProductSearch(null, null, null, "camisa"), PageQuery.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void softDeletePersisteDeletedAt() {
        Product product = repository.save(producto("CAMISA", null, null, null));
        product.delete();

        repository.save(product);
        Product loaded = repository.findById(product.getId()).orElseThrow();

        assertThat(loaded.isDeleted()).isTrue();
        assertThat(loaded.getDeletedAt()).isPresent();
    }

    private static Product producto(String sku, String barcode, BrandId brandId, CategoryId categoria) {
        Product product = Product.create(ProductId.generate(), new Sku(sku),
                barcode == null ? null : new Barcode(barcode), "Producto " + sku, "Descripción", brandId);
        if (categoria != null) {
            product.associateCategory(categoria);
        }
        product.addAttribute("Color", "Negro");
        product.addImage("https://img.example.com/a.jpg");
        product.addVariant(new Sku(sku + "-S"), null, List.of(new ProductAttribute("Talla", "S")));
        return product;
    }
}