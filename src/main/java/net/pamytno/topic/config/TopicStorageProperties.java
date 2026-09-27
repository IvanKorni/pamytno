package net.pamytno.topic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.nio.file.Path;

/**
 * Файловое хранилище загруженных материалов ({@code pamytno.topic.storage}).
 * В Docker каталог монтируется как volume.
 *
 * @param root корневой каталог хранилища
 */
@ConfigurationProperties("pamytno.topic.storage")
public record TopicStorageProperties(@DefaultValue("./storage") Path root) {
}
