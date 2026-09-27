package net.pamytno.topic.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link YoutubeVideoId}.
 */
class YoutubeVideoIdTest {

    @ParameterizedTest
    @DisplayName("Идентификатор извлекается из всех популярных форматов ссылок")
    @ValueSource(strings = {
        "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
        "https://youtube.com/watch?feature=share&v=dQw4w9WgXcQ&t=42",
        "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
        "https://youtu.be/dQw4w9WgXcQ",
        "https://www.youtube.com/shorts/dQw4w9WgXcQ",
        "https://www.youtube.com/embed/dQw4w9WgXcQ",
        "  https://www.youtube.com/live/dQw4w9WgXcQ  "
    })
    void fromUrl_extractsId(String url) {
        assertThat(YoutubeVideoId.fromUrl(url)).contains(new YoutubeVideoId("dQw4w9WgXcQ"));
    }

    @ParameterizedTest
    @DisplayName("Не-YouTube ссылки и мусор отклоняются")
    @ValueSource(strings = {
        "https://vimeo.com/watch?v=dQw4w9WgXcQ",
        "https://www.youtube.com/watch?v=short",
        "https://www.youtube.com/channel/UC123",
        "https://evil-youtube.com/watch?v=dQw4w9WgXcQ",
        "не ссылка",
        "http://[bad"
    })
    void fromUrl_rejectsOtherLinks(String url) {
        assertThat(YoutubeVideoId.fromUrl(url)).isEmpty();
    }
}
