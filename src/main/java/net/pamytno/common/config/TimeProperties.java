package net.pamytno.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.ZoneId;

/**
 * Настройки времени приложения ({@code pamytno.time}).
 *
 * @param zone часовой пояс, в котором считаются «сегодня» и границы суток
 */
@ConfigurationProperties("pamytno.time")
public record TimeProperties(@DefaultValue("UTC") ZoneId zone) {
}
