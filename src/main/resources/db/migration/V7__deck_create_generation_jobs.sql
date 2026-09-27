-- Модуль deck: асинхронные задачи генерации вопросов и карточек.
CREATE TABLE deck.generation_jobs
(
    id            UUID PRIMARY KEY,
    topic_id      UUID                     NOT NULL,
    user_id       UUID                     NOT NULL,
    type          VARCHAR(20)              NOT NULL,
    status        VARCHAR(20)              NOT NULL,
    items_created INTEGER                  NOT NULL,
    error_message VARCHAR(1000),
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX generation_jobs_topic_type_status_idx ON deck.generation_jobs (topic_id, type, status);
