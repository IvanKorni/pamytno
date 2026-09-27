-- Модуль deck: вопросы, предложенные AI по фрагментам единого текста.
CREATE TABLE deck.questions
(
    id              UUID PRIMARY KEY,
    topic_id        UUID                     NOT NULL,
    user_id         UUID                     NOT NULL,
    chunk_id        UUID REFERENCES deck.text_chunks (id) ON DELETE SET NULL,
    text            TEXT                     NOT NULL,
    source_fragment TEXT,
    status          VARCHAR(20)              NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX questions_topic_created_idx ON deck.questions (topic_id, created_at);
