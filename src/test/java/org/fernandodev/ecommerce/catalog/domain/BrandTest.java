package org.fernandodev.ecommerce.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class BrandTest {

    @Test
    void crearCreaMarcaActiva() {
        Brand brand = Brand.create(BrandId.generate(), "Nike");

        assertThat(brand.getName()).isEqualTo("Nike");
        assertThat(brand.isActive()).isTrue();
        assertThat(brand.isDeleted()).isFalse();
    }

    @Test
    void nombreObligatorioEsRechazado() {
        assertThatThrownBy(() -> Brand.create(BrandId.generate(), null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void activarYDesactivarCambiaElEstado() {
        Brand brand = Brand.create(BrandId.generate(), "Nike");

        brand.deactivate();
        assertThat(brand.isActive()).isFalse();

        brand.activate();
        assertThat(brand.isActive()).isTrue();
    }

    @Test
    void eliminarMarcaSoftDelete() {
        Brand brand = Brand.create(BrandId.generate(), "Nike");

        brand.delete();

        assertThat(brand.isDeleted()).isTrue();
        assertThat(brand.getDeletedAt()).isPresent();
    }

    @Test
    void eliminarMarcaYaEliminadaLanzaExcepcion() {
        Brand brand = Brand.create(BrandId.generate(), "Nike");
        brand.delete();

        assertThatThrownBy(brand::delete)
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void actualizarNombreCambiaElValor() {
        Brand brand = Brand.create(BrandId.generate(), "Nike");

        brand.updateName("Adidas");

        assertThat(brand.getName()).isEqualTo("Adidas");
    }
}