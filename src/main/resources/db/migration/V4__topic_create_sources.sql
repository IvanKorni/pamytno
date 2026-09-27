-- Модуль topic: источники материалов темы.
-- original_* хранят исходный материал и никогда не перезаписываются,
-- extracted_text — текст после извлечения и технической очистки.
CREATE TABLE topic.sources
(
    id             UUID PRIMARY KEY,
    topic_id       UUID                     NOT NULL REFERENCES topic.topics (id) ON DELETE CASCADE,
    user_id        UUID                     NOT NULL,
    type           VARCHAR(20)              NOT NULL,
    status         VARCHAR(20)              NOT NULL,
    original_name  VARCHAR(255),
    original_url   VARCHAR(2048),
    original_text  TEXT,
    storage_key    VARCHAR(500),
    extracted_text TEXT,
    error_code     VARCHAR(50),
    error_message  VARCHAR(1000),
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX sources_topic_created_idx ON topic.sources (topic_id, created_at);
CREATE INDEX sources_user_idx ON topic.sources (user_id);
