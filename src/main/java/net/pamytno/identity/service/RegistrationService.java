package net.pamytno.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.identity.config.IdentityProperties;
import net.pamytno.identity.domain.User;
import net.pamytno.identity.exception.EmailAlreadyRegisteredException;
import net.pamytno.identity.exception.RegistrationDisabledException;
import net.pamytno.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Регистрирует пользователя и сразу выдаёт ему токен.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenIssuer tokenIssuer;
    private final IdentityProperties properties;
    private final Clock clock;

    /**
     * Регистрирует пользователя.
     *
     * @param email    email
     * @param password пароль в открытом виде (не логируется и не хранится)
     * @return токен нового пользователя
     * @throws RegistrationDisabledException    если регистрация выключена
     * @throws EmailAlreadyRegisteredException если email занят
     */
    @Transactional
    public AccessToken register(String email, String password) {
        requireRegistrationEnabled();
        var normalizedEmail = User.normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyRegisteredException();
        }
        var user = userRepository.save(User.register(normalizedEmail, passwordEncoder.encode(password),
                clock.instant()));
        log.info("Пользователь [{}] зарегистрирован", user.getId());
        return tokenIssuer.issue(user);
    }

    /**
     * Проверяет, что регистрация разрешена настройками.
     */
    private void requireRegistrationEnabled() {
        if (!properties.registrationEnabled()) {
            throw new RegistrationDisabledException();
        }
    }
}
