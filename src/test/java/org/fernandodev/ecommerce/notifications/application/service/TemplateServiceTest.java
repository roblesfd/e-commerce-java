package org.fernandodev.ecommerce.notifications.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.fernandodev.ecommerce.notifications.application.dto.UpdateTemplateCommand;
import org.fernandodev.ecommerce.notifications.application.port.out.TemplateRepository;
import org.fernandodev.ecommerce.notifications.domain.NotificationTemplate;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationTemplateId;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class TemplateServiceTest {

    private final FakeTemplateRepository repository = new FakeTemplateRepository();
    private final TemplateService service = new TemplateService(repository);

    @Test
    void createGuardaPlantilla() {
        NotificationTemplate template = service.create("ORDER_SHIPPED", "Enviado", "En camino");

        assertThat(template.getCode()).isEqualTo("ORDER_SHIPPED");
        assertThat(template.isActive()).isTrue();
        assertThat(repository.findById(template.getId())).contains(template);
    }

    @Test
    void listDevuelveTodas() {
        service.create("A", "T", "B");
        service.create("B", "T", "B");

        assertThat(service.list()).hasSize(2);
    }

    @Test
    void updateCambiaTituloYBody() {
        NotificationTemplate template = service.create("ORDER_SHIPPED", "Enviado", "En camino");

        NotificationTemplate updated = service.update(template.getId(),
                new UpdateTemplateCommand("Entregado", "Entregado correctamente"));

        assertThat(updated.getTitle()).isEqualTo("Entregado");
        assertThat(updated.getBody()).isEqualTo("Entregado correctamente");
    }

    @Test
    void activateYDeactivateCambianElEstado() {
        NotificationTemplate template = service.create("ORDER_SHIPPED", "Enviado", "En camino");

        NotificationTemplate desactivada = service.deactivate(template.getId());
        assertThat(desactivada.isActive()).isFalse();

        NotificationTemplate activada = service.activate(template.getId());
        assertThat(activada.isActive()).isTrue();
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(NotificationTemplateId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static final class FakeTemplateRepository implements TemplateRepository {

        private final Map<NotificationTemplateId, NotificationTemplate> byId = new HashMap<>();

        @Override
        public NotificationTemplate save(NotificationTemplate template) {
            byId.put(template.getId(), template);
            return template;
        }

        @Override
        public Optional<NotificationTemplate> findById(NotificationTemplateId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<NotificationTemplate> findByCode(String code) {
            return byId.values().stream().filter(t -> t.getCode().equals(code)).findFirst();
        }

        @Override
        public List<NotificationTemplate> findAll() {
            return List.copyOf(byId.values());
        }
    }
}