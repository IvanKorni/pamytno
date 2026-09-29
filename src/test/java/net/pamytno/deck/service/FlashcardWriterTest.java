package net.pamytno.deck.service;

import net.pamytno.common.event.deck.FlashcardCreated;
import net.pamytno.deck.DeckFixtures;
import net.pamytno.deck.domain.Flashcard;
import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.QuestionStatus;
import net.pamytno.deck.domain.WordCard;
import net.pamytno.deck.domain.WordEntry;
import net.pamytno.deck.integration.ai.GeneratedCard;
import net.pamytno.deck.repository.FlashcardRepository;
import net.pamytno.deck.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link FlashcardWriter}.
 */
@ExtendWith(MockitoExtension.class)
class FlashcardWriterTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private FlashcardRepository flashcardRepository;
    @Mock
    private ApplicationEventPublisher events;

    private FlashcardWriter writer;
    private Question question;

    @BeforeEach
    void setUp() {
        writer = new FlashcardWriter(questionRepository, flashcardRepository, events,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        question = new Question(DeckFixtures.chunk(DeckFixtures.topic(), "JVM."), "Что такое JVM?", "JVM.",
                Instant.EPOCH);
        question.approve(Instant.EPOCH);
    }

    @Test
    @DisplayName("Карточка сохраняется, вопрос получает CARD_CREATED, публикуется FlashcardCreated")
    void create_savesCardAndPublishesEvent() {
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));
        when(flashcardRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var created = writer.create(question.getId(), new GeneratedCard(" ", " Виртуальная машина. "));

        var card = ArgumentCaptor.forClass(Flashcard.class);
        verify(flashcardRepository).save(card.capture());
        assertThat(created).isTrue();
        assertThat(card.getValue().getFront()).isEqualTo("Что такое JVM?");
        assertThat(card.getValue().getBack()).isEqualTo("Виртуальная машина.");
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.CARD_CREATED);
        verify(events).publishEvent(new FlashcardCreated(card.getValue().getId(), question.getTopicId(),
                question.getUserId(), "Что такое JVM?", "Виртуальная машина."));
    }

    @Test
    @DisplayName("Если вопрос успели отклонить, карточка не создаётся")
    void create_skips_whenQuestionNoLongerApproved() {
        question.reject(Instant.EPOCH);
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));

        assertThat(writer.create(question.getId(), new GeneratedCard("Что?", "Ответ."))).isFalse();
        verify(flashcardRepository, never()).save(any());
        verify(events, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Пустой ответ модели не превращается в карточку")
    void create_skips_whenBackIsBlank() {
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));

        assertThat(writer.create(question.getId(), new GeneratedCard("Что?", "  "))).isFalse();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.APPROVED);
    }

    @Test
    @DisplayName("Карточка слова сохраняется без вопроса и публикует FlashcardCreated")
    void createWord_savesCardWithoutQuestion() {
        // given
        var topic = DeckFixtures.topic();
        var word = WordCard.from(new WordEntry("contract", "договор", "A formal agreement.", "We signed a contract.",
                "contract", "Мы подписали договор.")).orElseThrow();
        when(flashcardRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        writer.createWord(topic, word);

        // then
        var card = ArgumentCaptor.forClass(Flashcard.class);
        verify(flashcardRepository).save(card.capture());
        assertThat(card.getValue().getQuestionId()).isNull();
        assertThat(card.getValue().getUserId()).isEqualTo(topic.userId());
        verify(events).publishEvent(new FlashcardCreated(card.getValue().getId(), topic.topicId(), topic.userId(),
                word.front(), word.back()));
    }
}
