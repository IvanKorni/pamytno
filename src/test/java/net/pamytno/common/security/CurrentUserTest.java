package net.pamytno.common.security;

import net.pamytno.common.error.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-тесты {@link CurrentUser}.
 */
class CurrentUserTest {

    private final CurrentUser currentUser = new CurrentUser();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Идентификатор пользователя берётся из claim sub токена")
    void id_returnsSubjectOfJwt() {
        // given
        var userId = UUID.randomUUID();
        var jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject(userId.toString()).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        // when
        var result = currentUser.id();

        // then
        assertThat(result).isEqualTo(userId);
    }

    @Test
    @DisplayName("Без JWT-аутентификации выбрасывается UnauthorizedException")
    void id_throwsUnauthorized_whenNotJwtAuthentication() {
        // given
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "password"));

        // when / then
        assertThatThrownBy(currentUser::id).isInstanceOf(UnauthorizedException.class);
    }
}
