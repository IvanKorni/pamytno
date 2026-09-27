-- Модуль learning: прогресс интервального повторения карточек.
-- card_front/card_back — копия текста карточки из модуля deck (обновляется событиями).
CREATE SCHEMA IF NOT EXISTS learning;

CREATE TABLE learning.card_progress
(
    id                  UUID PRIMARY KEY,
    card_id             UUID                     NOT NULL,
    user_id             UUID                     NOT NULL,
    topic_id            UUID                     NOT NULL,
    card_front          TEXT                     NOT NULL,
    card_back           TEXT                     NOT NULL,
    stage               INTEGER                  NOT NULL,
    next_review_at      TIMESTAMP WITH TIME ZONE,
    last_review_at      TIMESTAMP WITH TIME ZONE,
    consecutive_success INTEGER                  NOT NULL,
    total_reviews       INTEGER                  NOT NULL,
    total_remembered    INTEGER                  NOT NULL,
    total_forgotten     INTEGER                  NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT card_progress_card_uq UNIQUE (card_id)
);

CREATE INDEX card_progress_due_idx ON learning.card_progress (user_id, topic_id, next_review_at);

COMMENT ON COLUMN learning.card_progress.next_review_at IS 'Когда повторить; NULL — карточка изучена (этап 6)';
