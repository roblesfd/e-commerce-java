-- Contexto Payments: soporte de reembolsos parciales acumulados.

ALTER TABLE payment ADD COLUMN refunded_amount NUMERIC(19,2);