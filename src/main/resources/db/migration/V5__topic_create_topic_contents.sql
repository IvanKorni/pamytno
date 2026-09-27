-- Модуль topic: версии единого текста темы (Master Text).
-- Новая версия создаётся, только когда объединённый текст готовых источников изменился.
CREATE TABLE topic.topic_contents
(
    id         UUID PRIMARY KEY,
    topic_id   UUID                     NOT NULL REFERENCES topic.topics (id) ON DELETE CASCADE,
    version    INTEGER                  NOT NULL,
    content    TEXT                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT topic_contents_topic_version_uq UNIQUE (topic_id, version)
);
