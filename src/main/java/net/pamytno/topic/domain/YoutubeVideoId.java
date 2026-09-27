package net.pamytno.topic.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Идентификатор видео YouTube, извлечённый из ссылки.
 * Поддерживаются {@code youtube.com/watch?v=}, {@code youtu.be/}, {@code /shorts/}, {@code /embed/}, {@code /live/}.
 *
 * @param value 11-символьный идентификатор видео
 */
public record YoutubeVideoId(String value) {

    private static final Pattern SHORT_LINK_PATH = Pattern.compile("^/([A-Za-z0-9_-]{11})$");
    private static final Pattern WATCH_QUERY = Pattern.compile("(?:^|&)v=([A-Za-z0-9_-]{11})(?:&|$)");
    private static final Pattern VIDEO_PATH = Pattern.compile("^/(?:shorts|embed|live)/([A-Za-z0-9_-]{11})");

    /**
     * Извлекает идентификатор видео из ссылки.
     *
     * @param url ссылка на видео
     * @return идентификатор, если ссылка ведёт на видео YouTube
     */
    public static Optional<YoutubeVideoId> fromUrl(String url) {
        try {
            var uri = new URI(url.strip());
            var host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            return extractId(host, uri).map(YoutubeVideoId::new);
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    /**
     * Находит идентификатор в пути или параметрах в зависимости от домена.
     *
     * @param host домен ссылки в нижнем регистре
     * @param uri  разобранная ссылка
     * @return идентификатор, если найден
     */
    private static Optional<String> extractId(String host, URI uri) {
        if ("youtu.be".equals(host)) {
            return firstGroup(SHORT_LINK_PATH, uri.getPath());
        }
        return isYoutubeHost(host)
                ? firstGroup(WATCH_QUERY, uri.getRawQuery()).or(() -> firstGroup(VIDEO_PATH, uri.getPath()))
                : Optional.empty();
    }

    /**
     * Проверяет домен YouTube, включая поддомены {@code www}, {@code m}, {@code music}.
     *
     * @param host домен в нижнем регистре
     * @return {@code true} для доменов YouTube
     */
    private static boolean isYoutubeHost(String host) {
        return "youtube.com".equals(host) || host.endsWith(".youtube.com");
    }

    /**
     * Первая группа регулярного выражения.
     *
     * @param pattern выражение с одной группой
     * @param input   строка, может быть {@code null}
     * @return значение группы, если выражение нашлось
     */
    private static Optional<String> firstGroup(Pattern pattern, String input) {
        var matcher = pattern.matcher(input == null ? "" : input);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
