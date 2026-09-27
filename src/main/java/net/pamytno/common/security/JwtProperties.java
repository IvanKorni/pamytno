package net.pamytno.common.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Настройки JWT ({@code pamytno.security.jwt}). Секрет задаётся только через переменную окружения
 * {@code JWT_SECRET}.
 *
 * @param secret секрет HMAC-подписи, не короче 32 символов
 * @param ttl    время жизни access-токена
 * @param issuer издатель токена (claim {@code iss})
 */
@Validated
@ConfigurationProperties("pamytno.security.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32, message = "секрет JWT должен быть не короче 32 символов") String secret,
        @NotNull @DefaultValue("12h") Duration ttl,
        @NotBlank @DefaultValue("pamytno") String issuer
) {

    /** Алгоритм подписи токенов. */
    public static final String ALGORITHM = "HmacSHA256";

    /**
     * Строит ключ подписи из секрета.
     *
     * @return ключ HMAC-SHA256
     */
    public SecretKey secretKey() {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }
}
