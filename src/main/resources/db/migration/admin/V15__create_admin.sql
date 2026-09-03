-- Contexto Administration: auditoría y configuración del sistema.

CREATE TABLE audit_log (
    id          UUID         PRIMARY KEY,
    actor_id    UUID         NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    target_type VARCHAR(50),
    target_id   UUID,
    detail      VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_actor ON audit_log (actor_id);
CREATE INDEX idx_audit_action ON audit_log (action);

CREATE TABLE system_config (
    config_key VARCHAR(100) PRIMARY KEY,
    value      VARCHAR(500) NOT NULL,
    updated_at TIMESTAMP    NOT NULL DEFAULT now()
);