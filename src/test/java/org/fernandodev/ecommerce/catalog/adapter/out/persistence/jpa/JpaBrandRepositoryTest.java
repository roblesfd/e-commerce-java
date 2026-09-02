package org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.application.port.out.BrandRepository;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaBrandRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private BrandRepository repository;

    @Test
    void guardaYRecuperaPorId() {
        Brand brand = Brand.create(BrandId.generate(), "Nike");

        repository.save(brand);
        Brand loaded = repository.findById(brand.getId()).orElseThrow();

        assertThat(loaded.getId()).isEqualTo(brand.getId());
        assertThat(loaded.getName()).isEqualTo("Nike");
    }

    @Test
    void existsByIdExcluyeEliminadas() {
        Brand brand = repository.save(Brand.create(BrandId.generate(), "Nike"));

        assertThat(repository.existsById(brand.getId())).isTrue();

        brand.delete();
        repository.save(brand);

        assertThat(repository.existsById(brand.getId())).isFalse();
    }

    @Test
    void findAllExcluyeEliminadas() {
        repository.save(Brand.create(BrandId.generate(), "Nike"));
        Brand borrada = repository.save(Brand.create(BrandId.generate(), "Adidas"));
        borrada.delete();
        repository.save(borrada);

        assertThat(repository.findAll()).hasSize(1);
    }
}