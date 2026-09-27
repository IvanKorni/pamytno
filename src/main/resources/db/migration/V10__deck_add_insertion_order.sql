-- Модуль deck: порядок вставки для вопросов и карточек.
-- Вопросы и карточки создаются пачками с одинаковым created_at, поэтому порядок генерации
-- хранится отдельной identity-колонкой.
ALTER TABLE deck.questions ADD COLUMN seq BIGINT GENERATED ALWAYS AS IDENTITY;
ALTER TABLE deck.flashcards ADD COLUMN seq BIGINT GENERATED ALWAYS AS IDENTITY;

CREATE INDEX questions_topic_seq_idx ON deck.questions (topic_id, seq);
CREATE INDEX flashcards_topic_seq_idx ON deck.flashcards (topic_id, seq);
DROP INDEX deck.questions_topic_created_idx;
DROP INDEX deck.flashcards_topic_created_idx;
