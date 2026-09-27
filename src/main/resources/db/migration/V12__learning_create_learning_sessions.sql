-- Модуль learning: учебные сессии для статистики.
CREATE TABLE learning.learning_sessions
(
    id               UUID PRIMARY KEY,
    user_id          UUID                     NOT NULL,
    topic_id         UUID                     NOT NULL,
    started_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at     TIMESTAMP WITH TIME ZONE,
    cards_total      INTEGER                  NOT NULL,
    cards_remembered INTEGER                  NOT NULL,
    cards_forgotten  INTEGER                  NOT NULL
);

CREATE INDEX learning_sessions_user_topic_idx ON learning.learning_sessions (user_id, topic_id, started_at);
