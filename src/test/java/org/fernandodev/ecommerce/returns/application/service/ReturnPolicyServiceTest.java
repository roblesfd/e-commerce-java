package org.fernandodev.ecommerce.returns.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.returns.application.dto.CreateReturnPolicyCommand;
import org.fernandodev.ecommerce.returns.application.port.out.ReturnPolicyRepository;
import org.fernandodev.ecommerce.returns.domain.ReturnPolicy;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnPolicyId;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class ReturnPolicyServiceTest {

    private final FakeReturnPolicyRepository repository = new FakeReturnPolicyRepository();
    private final ReturnPolicyService service = new ReturnPolicyService(repository);

    @Test
    void createGuardaPolitica() {
        ReturnPolicy policy = service.create(new CreateReturnPolicyCommand(30, Set.of()));

        assertThat(policy.getId()).isNotNull();
        assertThat(policy.isActive()).isTrue();
        assertThat(repository.findById(policy.getId())).contains(policy);
    }

    @Test
    void listDevuelveTodas() {
        service.create(new CreateReturnPolicyCommand(30, Set.of()));
        service.create(new CreateReturnPolicyCommand(15, Set.of(ProductId.generate())));

        assertThat(service.list()).hasSize(2);
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(ReturnPolicyId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void activateYDeactivateCambianElEstado() {
        ReturnPolicy policy = service.create(new CreateReturnPolicyCommand(30, Set.of()));

        ReturnPolicy desactivada = service.deactivate(policy.getId());
        assertThat(desactivada.isActive()).isFalse();

        ReturnPolicy activada = service.activate(policy.getId());
        assertThat(activada.isActive()).isTrue();
    }

    private static final class FakeReturnPolicyRepository implements ReturnPolicyRepository {

        private final Map<ReturnPolicyId, ReturnPolicy> byId = new HashMap<>();

        @Override
        public ReturnPolicy save(ReturnPolicy policy) {
            byId.put(policy.getId(), policy);
            return policy;
        }

        @Override
        public Optional<ReturnPolicy> findById(ReturnPolicyId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<ReturnPolicy> findActive() {
            return byId.values().stream().filter(ReturnPolicy::isActive).findFirst();
        }

        @Override
        public List<ReturnPolicy> findAll() {
            return List.copyOf(byId.values());
        }
    }
}