-- Модуль deck: локальная проекция единого текста темы.
-- topic_materials — последняя известная версия текста темы (из события TopicContentPrepared),
-- text_chunks — её фрагменты для генерации вопросов. Старые версии фрагментов сохраняются:
-- на них ссылаются вопросы.
CREATE SCHEMA IF NOT EXISTS deck;

CREATE TABLE deck.topic_materials
(
    id              UUID PRIMARY KEY,
    user_id         UUID                     NOT NULL,
    content_version INTEGER                  NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON COLUMN deck.topic_materials.id IS 'Идентификатор темы (topic.topics.id), без внешнего ключа между схемами';

CREATE TABLE deck.text_chunks
(
    id              UUID PRIMARY KEY,
    topic_id        UUID                     NOT NULL,
    user_id         UUID                     NOT NULL,
    content_version INTEGER                  NOT NULL,
    sequence_number INTEGER                  NOT NULL,
    start_offset    INTEGER                  NOT NULL,
    end_offset      INTEGER                  NOT NULL,
    content         TEXT                     NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT text_chunks_version_sequence_uq UNIQUE (topic_id, content_version, sequence_number)
);
