package net.pamytno.deck.integration.ai;

/**
 * Вопрос, предложенный моделью.
 *
 * @param text           текст вопроса
 * @param sourceFragment дословная цитата из материала, на которой основан вопрос
 */
public record GeneratedQuestion(String text, String sourceFragment) {
}
