package org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.returns.application.port.out.ReturnPolicyRepository;
import org.fernandodev.ecommerce.returns.domain.ReturnPolicy;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnPolicyId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaReturnPolicyRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ReturnPolicyRepository repository;

    @Test
    void guardaYRecuperaPoliticaConProductos() {
        ProductId product = ProductId.generate();
        ReturnPolicy policy = ReturnPolicy.create(ReturnPolicyId.generate(), 30, Set.of(product));

        repository.save(policy);
        ReturnPolicy loaded = repository.findById(policy.getId()).orElseThrow();

        assertThat(loaded.getReturnWindowDays()).isEqualTo(30);
        assertThat(loaded.getReturnableProductIds()).contains(product);
        assertThat(loaded.isActive()).isTrue();
    }

    @Test
    void findActiveDevuelveLaActiva() {
        repository.save(ReturnPolicy.create(ReturnPolicyId.generate(), 30, Set.of()));
        ReturnPolicy desactivada = repository.save(ReturnPolicy.create(ReturnPolicyId.generate(), 15, Set.of()));
        desactivada.deactivate();
        repository.save(desactivada);

        assertThat(repository.findActive()).isPresent();
        assertThat(repository.findActive().orElseThrow().getReturnWindowDays()).isEqualTo(30);
    }
}