-- Модуль identity: пользователи приложения.
CREATE SCHEMA IF NOT EXISTS identity;

CREATE TABLE identity.users
(
    id            UUID PRIMARY KEY,
    email         VARCHAR(255)             NOT NULL,
    password_hash VARCHAR(100)             NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT users_email_uq UNIQUE (email)
);

COMMENT ON TABLE identity.users IS 'Пользователи; email хранится в нижнем регистре';
