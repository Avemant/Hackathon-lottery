CREATE TABLE IF NOT EXISTS draws (
    id              BIGSERIAL PRIMARY KEY,
    status          VARCHAR(20) NOT NULL,
    winning_numbers INTEGER[],
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ,
    CONSTRAINT draws_status_check CHECK (status IN ('ACTIVE', 'COMPLETED'))
);

CREATE TABLE IF NOT EXISTS tickets (
    id         BIGSERIAL PRIMARY KEY,
    draw_id    BIGINT NOT NULL REFERENCES draws (id) ON DELETE RESTRICT,
    numbers    INTEGER[] NOT NULL,
    status     VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT tickets_status_check CHECK (status IN ('PENDING', 'WIN', 'LOSE'))
);

CREATE INDEX IF NOT EXISTS idx_draws_status ON draws (status);
CREATE INDEX IF NOT EXISTS idx_tickets_draw_id ON tickets (draw_id);
CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets (status);
