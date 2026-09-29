package net.pamytno.deck;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.event.topic.TopicContentPrepared;
import net.pamytno.common.event.topic.TopicCreated;
import net.pamytno.deck.domain.TopicMaterial;
import net.pamytno.deck.repository.TopicMaterialRepository;
import net.pamytno.deck.domain.TextChunk;
import net.pamytno.deck.service.TopicMaterialService;
import net.pamytno.support.ModuleTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestConstructor;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Модульный тест {@code deck}: тема регистрируется по {@link TopicCreated}, фрагменты строятся по событию
 * {@link TopicContentPrepared} без участия модуля {@code topic}.
 */
@ModuleTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TopicMaterialModuleTest {

    private final TopicMaterialService topicMaterialService;
    private final TopicMaterialRepository topicMaterialRepository;

    @Test
    @DisplayName("Событие о тексте темы превращается во фрагменты последней версии")
    void contentPrepared_buildsLatestChunks(Scenario scenario) {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        Supplier<List<TextChunk>> chunks = () -> topicMaterialService.latestChunks(topicId, userId);

        scenario.publish(new TopicContentPrepared(topicId, userId, 1, "Первый текст."))
                .andWaitForStateChange(chunks, list -> !list.isEmpty());
        scenario.publish(new TopicContentPrepared(topicId, userId, 2, "Второй текст."))
                .andWaitForStateChange(chunks, list -> list.stream().anyMatch(chunk -> chunk.getContentVersion() == 2));

        assertThat(topicMaterialService.latestChunks(topicId, userId))
                .extracting(TextChunk::getContent).containsExactly("Второй текст.");
    }

    @Test
    @DisplayName("Фрагменты чужой темы не выдаются")
    void latestChunks_areIsolatedByOwner(Scenario scenario) {
        var topicId = UUID.randomUUID();
        var owner = UUID.randomUUID();

        Supplier<List<TextChunk>> chunks = () -> topicMaterialService.latestChunks(topicId, owner);

        scenario.publish(new TopicContentPrepared(topicId, owner, 1, "Текст владельца."))
                .andWaitForStateChange(chunks, list -> !list.isEmpty());

        assertThat(topicMaterialService.latestChunks(topicId, UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Созданная тема известна модулю до текста, а пришедший текст делится на фрагменты")
    void topicCreated_registersTopicBeforeContent(Scenario scenario) {
        // given
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        // when
        scenario.publish(new TopicCreated(topicId, userId))
                .andWaitForStateChange(() -> topicMaterialRepository.findByIdAndUserId(topicId, userId),
                        found -> found.isPresent());

        // then
        assertThat(topicMaterialRepository.findByIdAndUserId(topicId, userId)).get()
                .extracting(TopicMaterial::getContentVersion).isEqualTo(TopicMaterial.NO_CONTENT);
        assertThat(topicMaterialService.latestChunks(topicId, userId)).isEmpty();
        scenario.publish(new TopicContentPrepared(topicId, userId, 1, "Первый текст."))
                .andWaitForStateChange(() -> topicMaterialService.latestChunks(topicId, userId),
                        list -> !list.isEmpty());
    }
}
