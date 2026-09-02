package org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.application.port.out.CategoryRepository;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaCategoryRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository repository;

    @Test
    void guardaYRecuperaPorId() {
        Category category = Category.create(CategoryId.generate(), "Electrónica", null);

        repository.save(category);
        Category loaded = repository.findById(category.getId()).orElseThrow();

        assertThat(loaded.getId()).isEqualTo(category.getId());
        assertThat(loaded.getName()).isEqualTo("Electrónica");
    }

    @Test
    void persisteJerarquiaPadre() {
        Category padre = repository.save(Category.create(CategoryId.generate(), "Electrónica", null));
        Category hija = repository.save(Category.create(CategoryId.generate(), "Laptops", padre.getId()));

        Category loaded = repository.findById(hija.getId()).orElseThrow();

        assertThat(loaded.getParentId()).contains(padre.getId());
    }

    @Test
    void existsByIdExcluyeEliminadas() {
        Category category = repository.save(Category.create(CategoryId.generate(), "Electrónica", null));

        assertThat(repository.existsById(category.getId())).isTrue();

        category.delete();
        repository.save(category);

        assertThat(repository.existsById(category.getId())).isFalse();
    }

    @Test
    void existsByParentIdDetectaSubcategorias() {
        Category padre = repository.save(Category.create(CategoryId.generate(), "Electrónica", null));
        repository.save(Category.create(CategoryId.generate(), "Laptops", padre.getId()));

        assertThat(repository.existsByParentId(padre.getId())).isTrue();
        assertThat(repository.existsByParentId(CategoryId.generate())).isFalse();
    }

    @Test
    void findAllExcluyeEliminadas() {
        repository.save(Category.create(CategoryId.generate(), "Electrónica", null));
        Category borrada = repository.save(Category.create(CategoryId.generate(), "Ropa", null));
        borrada.delete();
        repository.save(borrada);

        assertThat(repository.findAll()).hasSize(1);
    }
}