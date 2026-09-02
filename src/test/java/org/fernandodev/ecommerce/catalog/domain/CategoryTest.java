package org.fernandodev.ecommerce.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class CategoryTest {

    @Test
    void crearCreaCategoriaActivaSinPadre() {
        Category category = Category.create(CategoryId.generate(), "Electrónica", null);

        assertThat(category.getName()).isEqualTo("Electrónica");
        assertThat(category.isActive()).isTrue();
        assertThat(category.getParentId()).isEmpty();
        assertThat(category.isDeleted()).isFalse();
    }

    @Test
    void nombreObligatorioEsRechazado() {
        assertThatThrownBy(() -> Category.create(CategoryId.generate(), "  ", null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void soportaJerarquia() {
        CategoryId raiz = CategoryId.generate();
        Category categoria = Category.create(CategoryId.generate(), "Laptops", raiz);

        assertThat(categoria.getParentId()).contains(raiz);
    }

    @Test
    void moverBajoSuPropioIdLanzaExcepcion() {
        CategoryId id = CategoryId.generate();
        Category categoria = Category.create(id, "Laptops", null);

        assertThatThrownBy(() -> categoria.moveTo(id))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void moverACategoriaValidaCambiaElPadre() {
        CategoryId nuevoPadre = CategoryId.generate();
        Category categoria = Category.create(CategoryId.generate(), "Laptops", null);

        categoria.moveTo(nuevoPadre);

        assertThat(categoria.getParentId()).contains(nuevoPadre);
    }

    @Test
    void activarYDesactivarCambiaElEstado() {
        Category category = Category.create(CategoryId.generate(), "Electrónica", null);

        category.deactivate();
        assertThat(category.isActive()).isFalse();

        category.activate();
        assertThat(category.isActive()).isTrue();
    }

    @Test
    void eliminarMarcaSoftDelete() {
        Category category = Category.create(CategoryId.generate(), "Electrónica", null);

        category.delete();

        assertThat(category.isDeleted()).isTrue();
        assertThat(category.getDeletedAt()).isPresent();
    }

    @Test
    void eliminarCategoriaYaEliminadaLanzaExcepcion() {
        Category category = Category.create(CategoryId.generate(), "Electrónica", null);
        category.delete();

        assertThatThrownBy(category::delete)
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void actualizarNombreCambiaElValor() {
        Category category = Category.create(CategoryId.generate(), "Electrónica", null);

        category.updateName("Tecnología");

        assertThat(category.getName()).isEqualTo("Tecnología");
    }
}