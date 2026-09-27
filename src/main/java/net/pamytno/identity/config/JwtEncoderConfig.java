package net.pamytno.identity.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import net.pamytno.common.security.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Выпуск JWT подписью HMAC-SHA256 тем же секретом, которым их проверяет {@code common.security}.
 */
@Configuration
public class JwtEncoderConfig {

    /**
     * Кодировщик токенов.
     *
     * @param properties настройки JWT
     * @return кодировщик на основе Nimbus
     */
    @Bean
    public JwtEncoder jwtEncoder(JwtProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(properties.secretKey()));
    }
}
