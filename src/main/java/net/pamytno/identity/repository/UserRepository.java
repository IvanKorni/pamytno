package net.pamytno.identity.repository;

import net.pamytno.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Хранилище пользователей.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Ищет пользователя по нормализованному email.
     *
     * @param email email в нижнем регистре
     * @return пользователь, если найден
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверяет, занят ли email.
     *
     * @param email email в нижнем регистре
     * @return {@code true}, если пользователь с таким email уже есть
     */
    boolean existsByEmail(String email);
}
