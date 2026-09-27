package net.pamytno.identity.service;

import net.pamytno.common.security.JwtDecoderConfig;
import net.pamytno.common.security.JwtProperties;
import net.pamytno.identity.config.JwtEncoderConfig;
import net.pamytno.identity.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link AccessTokenIssuer}: выпущенный токен проходит проверку декодером из {@code common}.
 */
class AccessTokenIssuerTest {

    private static final JwtProperties PROPERTIES =
            new JwtProperties("unit-test-secret-0123456789abcdef-0123", Duration.ofHours(2), "pamytno");

    private final Instant now = Instant.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS);
    private final AccessTokenIssuer issuer = new AccessTokenIssuer(
            new JwtEncoderConfig().jwtEncoder(PROPERTIES), PROPERTIES, Clock.fixed(now, ZoneOffset.UTC));

    @Test
    @DisplayName("Токен содержит идентификатор пользователя, email, издателя и срок жизни")
    void issue_createsVerifiableToken() {
        // given
        var user = User.register("student@example.com", "hash", now);

        // when
        var token = issuer.issue(user);
        var jwt = new JwtDecoderConfig().jwtDecoder(PROPERTIES).decode(token.accessToken());

        // then
        assertThat(token.userId()).isEqualTo(user.getId());
        assertThat(token.expiresIn()).isEqualTo(Duration.ofHours(2).toSeconds());
        assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
        assertThat(jwt.getClaimAsString("email")).isEqualTo("student@example.com");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("pamytno");
        assertThat(jwt.getExpiresAt()).isEqualTo(now.plus(Duration.ofHours(2)));
    }
}
