package net.pamytno.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Настройки CORS ({@code pamytno.security.cors}).
 *
 * @param allowedOrigins шаблоны разрешённых origin фронтенда
 */
@ConfigurationProperties("pamytno.security.cors")
public record CorsProperties(@DefaultValue("http://localhost:*") List<String> allowedOrigins) {
}
