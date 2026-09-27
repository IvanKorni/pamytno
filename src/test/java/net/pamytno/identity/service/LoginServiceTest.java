package net.pamytno.identity.service;

import net.pamytno.identity.domain.User;
import net.pamytno.identity.exception.InvalidCredentialsException;
import net.pamytno.identity.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link LoginService}.
 */
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final User USER = User.register("student@example.com", "hash", Instant.EPOCH);

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AccessTokenIssuer tokenIssuer;
    @InjectMocks
    private LoginService loginService;

    @Test
    @DisplayName("Верные email и пароль дают токен; email ищется в нормализованном виде")
    void login_returnsToken_whenCredentialsValid() {
        // given
        var token = new AccessToken("jwt", 60, USER.getId());
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(USER));
        when(passwordEncoder.matches("secret-password", "hash")).thenReturn(true);
        when(tokenIssuer.issue(USER)).thenReturn(token);

        // when
        var result = loginService.login(" Student@Example.com", "secret-password");

        // then
        assertThat(result).isEqualTo(token);
    }

    @Test
    @DisplayName("Неверный пароль отклоняется с INVALID_CREDENTIALS")
    void login_throwsUnauthorized_whenPasswordWrong() {
        // given
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(USER));
        when(passwordEncoder.matches("wrong-password", "hash")).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> loginService.login("student@example.com", "wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Неизвестный email отклоняется с INVALID_CREDENTIALS")
    void login_throwsUnauthorized_whenUserUnknown() {
        // given
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> loginService.login("nobody@example.com", "secret-password"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
