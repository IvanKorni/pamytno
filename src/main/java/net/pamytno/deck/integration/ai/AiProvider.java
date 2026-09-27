package net.pamytno.deck.integration.ai;

import java.util.List;

/**
 * Провайдер языковой модели. Бизнес-логика зависит только от этого интерфейса,
 * поэтому провайдера (Claude, OpenAI, Gemini, локальная модель) можно заменить настройкой
 * {@code pamytno.deck.ai.provider}.
 */
public interface AiProvider {

    /**
     * Предлагает вопросы для самопроверки по фрагменту учебного материала.
     *
     * @param text фрагмент единого текста темы
     * @return вопросы с цитатами, на которых они основаны
     * @throws AiGenerationException если модель недоступна или ответ непригоден
     */
    List<GeneratedQuestion> generateQuestions(String text);

    /**
     * Составляет карточку по вопросу и контексту из материала.
     *
     * @param question вопрос
     * @param context  фрагмент материала, на котором основан вопрос
     * @return лицевая и оборотная сторона карточки
     * @throws AiGenerationException если модель недоступна или ответ непригоден
     */
    GeneratedCard generateCard(String question, String context);
}
