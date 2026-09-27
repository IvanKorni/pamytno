package net.pamytno.identity.service;

import net.pamytno.identity.config.IdentityProperties;
import net.pamytno.identity.domain.User;
import net.pamytno.identity.exception.EmailAlreadyRegisteredException;
import net.pamytno.identity.exception.RegistrationDisabledException;
import net.pamytno.identity.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link RegistrationService}.
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-27T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AccessTokenIssuer tokenIssuer;

    @Test
    @DisplayName("Новый пользователь сохраняется с хешем пароля и получает токен")
    void register_savesUserAndIssuesToken() {
        // given
        var service = service(true);
        var token = new AccessToken("jwt", 60, UUID.randomUUID());
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret-password")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenIssuer.issue(any(User.class))).thenReturn(token);

        // when
        var result = service.register("Student@Example.com", "secret-password");

        // then
        var saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("student@example.com");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("hash");
        assertThat(result).isEqualTo(token);
    }

    @Test
    @DisplayName("Занятый email отклоняется с EMAIL_ALREADY_REGISTERED")
    void register_throwsConflict_whenEmailTaken() {
        // given
        var service = service(true);
        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> service.register("student@example.com", "secret-password"))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("При выключенной регистрации выбрасывается REGISTRATION_DISABLED")
    void register_throwsForbidden_whenRegistrationDisabled() {
        // given
        var service = service(false);

        // when / then
        assertThatThrownBy(() -> service.register("student@example.com", "secret-password"))
                .isInstanceOf(RegistrationDisabledException.class);
        verify(userRepository, never()).save(any());
    }

    /**
     * Собирает сервис с нужной настройкой регистрации.
     *
     * @param registrationEnabled разрешена ли регистрация
     * @return тестируемый сервис
     */
    private RegistrationService service(boolean registrationEnabled) {
        return new RegistrationService(userRepository, passwordEncoder, tokenIssuer,
                new IdentityProperties(registrationEnabled), CLOCK);
    }
}
