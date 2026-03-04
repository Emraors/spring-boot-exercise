CREATE TABLE IF NOT EXISTS payments (
    id          UUID        NOT NULL PRIMARY KEY,
    cents       BIGINT      NOT NULL,
    currency    VARCHAR(3)  NOT NULL,
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS idempotency_keys (
    idempotency_key VARCHAR(255) NOT NULL PRIMARY KEY,
    payment_id      UUID         NOT NULL REFERENCES payments(id)
);

