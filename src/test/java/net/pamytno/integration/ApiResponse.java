package net.pamytno.integration;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Ответ приложения в сквозном сценарии.
 *
 * @param status HTTP-статус
 * @param body   тело JSON или {@code null}, если тела нет
 */
record ApiResponse(int status, JsonNode body) {

    /**
     * Текстовое поле тела.
     *
     * @param field имя поля
     * @return значение поля
     */
    String text(String field) {
        return body.get(field).asText();
    }
}
