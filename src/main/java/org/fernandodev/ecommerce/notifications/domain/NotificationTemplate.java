package org.fernandodev.ecommerce.notifications.domain;

import java.time.Instant;
import java.util.Objects;

import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationTemplateId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Plantilla de notificación reutilizable por código.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationTemplate {

    private NotificationTemplateId id;
    private String code;
    private String title;
    private String body;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static NotificationTemplate create(NotificationTemplateId id, String code, String title, String body,
                                              Instant at) {
        NotificationTemplate template = new NotificationTemplate();
        template.id = Objects.requireNonNull(id, "id no puede ser null");
        template.code = validateCode(code);
        template.title = Objects.requireNonNull(title, "title no puede ser null");
        template.body = Objects.requireNonNull(body, "body no puede ser null");
        template.active = true;
        template.createdAt = Objects.requireNonNull(at, "at no puede ser null");
        template.updatedAt = at;
        return template;
    }

    public static NotificationTemplate crear(NotificationTemplateId id, String code, String title, String body,
                                             boolean active, Instant createdAt, Instant updatedAt) {
        NotificationTemplate template = new NotificationTemplate();
        template.id = Objects.requireNonNull(id, "id no puede ser null");
        template.code = validateCode(code);
        template.title = Objects.requireNonNull(title, "title no puede ser null");
        template.body = Objects.requireNonNull(body, "body no puede ser null");
        template.active = active;
        template.createdAt = Objects.requireNonNull(createdAt, "createdAt no puede ser null");
        template.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt no puede ser null");
        return template;
    }

    public void update(String title, String body, Instant at) {
        this.title = Objects.requireNonNull(title, "title no puede ser null");
        this.body = Objects.requireNonNull(body, "body no puede ser null");
        this.updatedAt = at;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    private static String validateCode(String code) {
        Objects.requireNonNull(code, "code no puede ser null");
        if (code.isBlank()) {
            throw new BusinessRuleViolationException("El código de la plantilla no puede estar vacío");
        }
        return code.trim();
    }
}