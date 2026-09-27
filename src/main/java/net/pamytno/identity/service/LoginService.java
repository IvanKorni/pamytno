package net.pamytno.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.identity.domain.User;
import net.pamytno.identity.exception.InvalidCredentialsException;
import net.pamytno.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Вход по email и паролю.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenIssuer tokenIssuer;

    /**
     * Проверяет учётные данные и выдаёт токен.
     *
     * @param email    email
     * @param password пароль в открытом виде
     * @return токен пользователя
     * @throws InvalidCredentialsException если пользователя нет или пароль не подходит
     */
    @Transactional(readOnly = true)
    public AccessToken login(String email, String password) {
        var user = userRepository.findByEmail(User.normalizeEmail(email))
                .filter(found -> passwordEncoder.matches(password, found.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        log.info("Пользователь [{}] вошёл в систему", user.getId());
        return tokenIssuer.issue(user);
    }
}
