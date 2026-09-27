-- Модуль topic: темы — наборы материалов пользователя.
CREATE SCHEMA IF NOT EXISTS topic;

CREATE TABLE topic.topics
(
    id          UUID PRIMARY KEY,
    user_id     UUID                     NOT NULL,
    title       VARCHAR(200)             NOT NULL,
    description VARCHAR(2000),
    status      VARCHAR(20)              NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX topics_user_created_idx ON topic.topics (user_id, created_at DESC);

COMMENT ON COLUMN topic.topics.user_id IS 'Владелец (identity.users.id), без внешнего ключа между схемами';
