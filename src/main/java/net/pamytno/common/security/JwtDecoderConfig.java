package net.pamytno.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Настраивает проверку входящих JWT: подпись HMAC-SHA256, срок действия и издатель.
 * Выпуск токенов — ответственность модуля {@code identity}.
 */
@Configuration
public class JwtDecoderConfig {

    /**
     * Декодер токенов для Spring Security Resource Server.
     *
     * @param properties настройки JWT
     * @return декодер, проверяющий подпись, срок и издателя
     */
    @Bean
    public JwtDecoder jwtDecoder(JwtProperties properties) {
        var decoder = NimbusJwtDecoder.withSecretKey(properties.secretKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
        return decoder;
    }
}
