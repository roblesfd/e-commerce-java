package org.fernandodev.ecommerce.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.fernandodev.ecommerce.catalog.domain.value_object.Barcode;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductAttribute;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariant;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.catalog.domain.value_object.Sku;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void crearCreaProductoActivoSinColecciones() {
        Product product = producto();

        assertThat(product.isActive()).isTrue();
        assertThat(product.getVariants()).isEmpty();
        assertThat(product.getAttributes()).isEmpty();
        assertThat(product.getImages()).isEmpty();
        assertThat(product.getCategoryIds()).isEmpty();
        assertThat(product.isDeleted()).isFalse();
    }

    @Test
    void skuSeNormalizaAMayusculas() {
        Product product = Product.create(ProductId.generate(), new Sku("  camisa-negra-s "), null,
                "Camisa", "Camisa negra", null);

        assertThat(product.getSku().value()).isEqualTo("CAMISA-NEGRA-S");
    }

    @Test
    void skuObligatorioEsRechazado() {
        assertThatThrownBy(() -> Product.create(ProductId.generate(), null, null, "Camisa", null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nombreObligatorioEsRechazado() {
        assertThatThrownBy(() -> Product.create(ProductId.generate(), new Sku("SKU-1"), null, "  ", null, null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void activarYDesactivarCambiaElEstado() {
        Product product = producto();

        product.deactivate();
        assertThat(product.isActive()).isFalse();

        product.activate();
        assertThat(product.isActive()).isTrue();
    }

    @Test
    void eliminarMarcaSoftDelete() {
        Product product = producto();

        product.delete();

        assertThat(product.isDeleted()).isTrue();
        assertThat(product.getDeletedAt()).isPresent();
    }

    @Test
    void eliminarProductoYaEliminadoLanzaExcepcion() {
        Product product = producto();
        product.delete();

        assertThatThrownBy(product::delete)
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void agregarVariantegeneraIdYListaCrece() {
        Product product = producto();

        ProductVariant variant = product.addVariant(new Sku("CAMISA-S"), null, List.of());

        assertThat(variant.id()).isNotNull();
        assertThat(product.getVariants()).hasSize(1);
        assertThat(product.getVariants().get(0).id()).isEqualTo(variant.id());
    }

    @Test
    void varianteConSkuDelProductoEsRechazada() {
        Product product = producto();

        assertThatThrownBy(() -> product.addVariant(product.getSku(), null, List.of()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void varianteConSkuDuplicadoEsRechazada() {
        Product product = producto();
        product.addVariant(new Sku("CAMISA-S"), null, List.of());

        assertThatThrownBy(() -> product.addVariant(new Sku("CAMISA-S"), null, List.of()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void varianteConBarcodeDuplicadoEsRechazada() {
        Product product = Product.create(ProductId.generate(), new Sku("CAMISA"), null,
                "Camisa", "Camisa de algodón", null);
        product.addVariant(new Sku("CAMISA-S"), new Barcode("7501234567890"), List.of());

        assertThatThrownBy(() -> product.addVariant(new Sku("CAMISA-M"), new Barcode("7501234567890"), List.of()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void actualizarVarianteMantieneElId() {
        Product product = producto();
        ProductVariant variant = product.addVariant(new Sku("CAMISA-S"), null, List.of());

        product.updateVariant(variant.id(), new Sku("CAMISA-S2"), null, List.of(new ProductAttribute("Talla", "S")));

        assertThat(product.getVariants()).hasSize(1);
        assertThat(product.getVariants().get(0).id()).isEqualTo(variant.id());
        assertThat(product.getVariants().get(0).sku()).isEqualTo(new Sku("CAMISA-S2"));
        assertThat(product.getVariants().get(0).attributes()).hasSize(1);
    }

    @Test
    void actualizarVarianteInexistenteLanzaExcepcion() {
        Product product = producto();

        assertThatThrownBy(() -> product.updateVariant(ProductVariantId.generate(),
                new Sku("CAMISA-S"), null, List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void eliminarVarianteReduceLaLista() {
        Product product = producto();
        ProductVariant variant = product.addVariant(new Sku("CAMISA-S"), null, List.of());

        product.removeVariant(variant.id());

        assertThat(product.getVariants()).isEmpty();
    }

    @Test
    void agregarAtributoReemplazaPorNombre() {
        Product product = producto();
        product.addAttribute("Color", "Negro");
        product.addAttribute("Color", "Blanco");

        assertThat(product.getAttributes()).hasSize(1);
        assertThat(product.getAttributes().get(0).value()).isEqualTo("Blanco");
    }

    @Test
    void eliminarAtributoQuitaPorNombre() {
        Product product = producto();
        product.addAttribute("Color", "Negro");

        product.removeAttribute("Color");

        assertThat(product.getAttributes()).isEmpty();
    }

    @Test
    void gestionDeImagenes() {
        Product product = producto();

        product.addImage("https://img.example.com/a.jpg");
        product.addImage("https://img.example.com/b.jpg");
        product.removeImage("https://img.example.com/a.jpg");

        assertThat(product.getImages()).hasSize(1);
        assertThat(product.getImages().get(0).url()).isEqualTo("https://img.example.com/b.jpg");

        product.setImages(List.of());
        assertThat(product.getImages()).isEmpty();
    }

    @Test
    void asociarYDesasociarCategorias() {
        Product product = producto();
        CategoryId categoria = CategoryId.generate();

        product.associateCategory(categoria);
        product.associateCategory(CategoryId.generate());

        assertThat(product.getCategoryIds()).hasSize(2);

        product.disassociateCategory(categoria);
        assertThat(product.getCategoryIds()).hasSize(1);
        assertThat(product.getCategoryIds()).doesNotContain(categoria);
    }

    @Test
    void asignarYQuitarMarca() {
        Product product = producto();
        BrandId marca = BrandId.generate();

        product.assignBrand(marca);
        assertThat(product.getBrandId()).isEqualTo(marca);

        product.removeBrand();
        assertThat(product.getBrandId()).isNull();
    }

    @Test
    void actualizarInfoBaseCambiaDatos() {
        Product product = producto();

        product.updateBaseInfo("Camisa Nueva", "Descripción nueva", new Barcode("7500000000001"));

        assertThat(product.getName()).isEqualTo("Camisa Nueva");
        assertThat(product.getDescription()).isEqualTo("Descripción nueva");
        assertThat(product.getBarcode()).isEqualTo(new Barcode("7500000000001"));
    }

    @Test
    void actualizarSkuCambiaElValor() {
        Product product = producto();

        product.updateSku(new Sku("CAMISA-NEGRA-2026"));

        assertThat(product.getSku()).isEqualTo(new Sku("CAMISA-NEGRA-2026"));
    }

    @Test
    void reemplazarAtributosSustituyeLaLista() {
        Product product = producto();
        product.addAttribute("Color", "Negro");

        product.setAttributes(List.of(new ProductAttribute("Material", "Algodón")));

        assertThat(product.getAttributes()).hasSize(1);
        assertThat(product.getAttributes().get(0).name()).isEqualTo("Material");
    }

    @Test
    void reemplazarVariantesSustituyeLaLista() {
        Product product = producto();
        product.addVariant(new Sku("CAMISA-S"), null, List.of());

        product.replaceVariants(List.of(
                new ProductVariant(ProductVariantId.generate(), new Sku("CAMISA-M"), null, List.of()),
                new ProductVariant(ProductVariantId.generate(), new Sku("CAMISA-L"), null, List.of())));

        assertThat(product.getVariants()).hasSize(2);
        assertThat(product.getVariants().stream().map(ProductVariant::sku).toList())
                .containsExactly(new Sku("CAMISA-M"), new Sku("CAMISA-L"));
    }

    @Test
    void reemplazarVariantesConSkuDelProductoEsRechazado() {
        Product product = producto();

        assertThatThrownBy(() -> product.replaceVariants(List.of(
                new ProductVariant(ProductVariantId.generate(), product.getSku(), null, List.of()))))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void reemplazarVariantesConSkuDuplicadoEsRechazado() {
        Product product = producto();

        assertThatThrownBy(() -> product.replaceVariants(List.of(
                new ProductVariant(ProductVariantId.generate(), new Sku("CAMISA-S"), null, List.of()),
                new ProductVariant(ProductVariantId.generate(), new Sku("CAMISA-S"), null, List.of()))))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private static Product producto() {
        return Product.create(ProductId.generate(), new Sku("CAMISA-NEGRA"), new Barcode("7501234567890"),
                "Camisa negra", "Camisa de algodón", null);
    }
}