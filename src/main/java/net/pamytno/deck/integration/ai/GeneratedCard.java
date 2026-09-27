package net.pamytno.deck.integration.ai;

/**
 * Карточка, составленная моделью.
 *
 * @param front вопрос на лицевой стороне
 * @param back  ответ на оборотной стороне
 */
public record GeneratedCard(String front, String back) {
}
