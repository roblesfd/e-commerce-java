-- Contexto Notifications: notificaciones, plantillas y preferencias de canal.

CREATE TABLE notification_template (
    id         UUID         PRIMARY KEY,
    code       VARCHAR(50)  NOT NULL UNIQUE,
    title      VARCHAR(150) NOT NULL,
    body       VARCHAR(2000) NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE notification (
    id               UUID         PRIMARY KEY,
    customer_id      UUID         NOT NULL,
    channel          VARCHAR(10)  NOT NULL,
    title            VARCHAR(150) NOT NULL,
    body             VARCHAR(2000) NOT NULL,
    status           VARCHAR(10)  NOT NULL,
    is_read          BOOLEAN      NOT NULL DEFAULT FALSE,
    delivery_attempts INT         NOT NULL DEFAULT 0,
    dedup_key        VARCHAR(100),
    sent_at          TIMESTAMP,
    failed_at        TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_notification_customer ON notification (customer_id);
CREATE INDEX idx_notification_customer_read ON notification (customer_id, is_read);

CREATE TABLE notification_preferences (
    customer_id    UUID      PRIMARY KEY,
    email_enabled  BOOLEAN   NOT NULL DEFAULT TRUE,
    sms_enabled    BOOLEAN   NOT NULL DEFAULT TRUE,
    push_enabled   BOOLEAN   NOT NULL DEFAULT TRUE,
    in_app_enabled BOOLEAN   NOT NULL DEFAULT TRUE,
    updated_at     TIMESTAMP NOT NULL DEFAULT now()
);