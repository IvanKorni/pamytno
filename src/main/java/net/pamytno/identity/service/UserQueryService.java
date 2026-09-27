package net.pamytno.identity.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.identity.domain.User;
import net.pamytno.identity.exception.UserNotFoundException;
import net.pamytno.identity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Чтение профиля пользователя.
 */
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return пользователь
     * @throws UserNotFoundException если пользователя нет
     */
    @Transactional(readOnly = true)
    public User getById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
}
