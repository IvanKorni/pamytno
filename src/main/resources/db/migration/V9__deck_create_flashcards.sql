-- Модуль deck: карточки для изучения. Текст карточки можно править, AI-карточка не неизменяема.
CREATE TABLE deck.flashcards
(
    id              UUID PRIMARY KEY,
    topic_id        UUID                     NOT NULL,
    user_id         UUID                     NOT NULL,
    question_id     UUID REFERENCES deck.questions (id) ON DELETE SET NULL,
    front           TEXT                     NOT NULL,
    back            TEXT                     NOT NULL,
    source_fragment TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX flashcards_topic_created_idx ON deck.flashcards (topic_id, created_at);
