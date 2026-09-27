package net.pamytno.deck;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.event.topic.TopicContentPrepared;
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
 * Модульный тест {@code deck}: фрагменты строятся по событию {@link TopicContentPrepared}
 * без участия модуля {@code topic}.
 */
@ModuleTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TopicMaterialModuleTest {

    private final TopicMaterialService topicMaterialService;

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
}
