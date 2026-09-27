package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.TextChunk;
import net.pamytno.deck.repository.TextChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Собирает контекст для карточки: цитата-источник вопроса и, если есть, весь фрагмент
 * единого текста, из которого она взята.
 */
@Service
@RequiredArgsConstructor
public class CardContextBuilder {

    /** Разделитель цитаты и дополнительного контекста. */
    public static final String SEPARATOR = "\n\n---\n\n";

    private final TextChunkRepository chunkRepository;

    /**
     * Контекст для вопроса.
     *
     * @param question вопрос
     * @return цитата, дополненная фрагментом, если он отличается от цитаты
     */
    @Transactional(readOnly = true)
    public String contextFor(Question question) {
        var fragment = question.getSourceFragment() == null ? "" : question.getSourceFragment();
        return Optional.ofNullable(question.getChunkId())
                .flatMap(chunkRepository::findById)
                .map(TextChunk::getContent)
                .filter(chunk -> !chunk.equals(fragment))
                .map(chunk -> fragment.isEmpty() ? chunk : fragment + SEPARATOR + chunk)
                .orElse(fragment);
    }
}
