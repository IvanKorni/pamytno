package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.TextChunk;
import net.pamytno.deck.integration.ai.GeneratedQuestion;
import net.pamytno.deck.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

/**
 * Сохраняет вопросы, предложенные AI по фрагменту. Пустые вопросы отбрасываются,
 * пустая цитата заменяется текстом фрагмента — связь с источником не теряется.
 */
@Service
@RequiredArgsConstructor
public class QuestionWriter {

    private final QuestionRepository questionRepository;
    private final Clock clock;

    /**
     * Сохраняет вопросы фрагмента.
     *
     * @param chunk     фрагмент, по которому они созданы
     * @param generated ответ модели
     * @return сколько вопросов сохранено
     */
    @Transactional
    public int save(TextChunk chunk, List<GeneratedQuestion> generated) {
        var now = clock.instant();
        var questions = generated.stream()
                .filter(question -> question.text() != null && !question.text().isBlank())
                .map(question -> new Question(chunk, question.text().strip(), fragmentOf(question, chunk), now))
                .toList();
        questionRepository.saveAll(questions);
        return questions.size();
    }

    /**
     * Цитата-источник вопроса.
     *
     * @param question ответ модели
     * @param chunk    фрагмент
     * @return цитата модели или текст фрагмента, если цитаты нет
     */
    private static String fragmentOf(GeneratedQuestion question, TextChunk chunk) {
        var fragment = question.sourceFragment();
        return fragment == null || fragment.isBlank() ? chunk.getContent() : fragment.strip();
    }
}
