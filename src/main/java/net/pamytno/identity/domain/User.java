package net.pamytno.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import net.pamytno.common.domain.BaseEntity;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Пользователь приложения. Email хранится нормализованным (без пробелов, в нижнем регистре),
 * пароль — только в виде BCrypt-хеша.
 */
@Getter
@Entity
@Table(name = "users", schema = "identity")
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Конструктор для JPA.
     */
    protected User() {
    }

    /**
     * Создаёт пользователя с заполненными полями.
     *
     * @param email        нормализованный email
     * @param passwordHash хеш пароля
     * @param now          момент регистрации
     */
    private User(String email, String passwordHash, Instant now) {
        super(UUID.randomUUID());
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param email        email в любом регистре
     * @param passwordHash хеш пароля
     * @param now          момент регистрации
     * @return новый пользователь
     */
    public static User register(String email, String passwordHash, Instant now) {
        return new User(normalizeEmail(email), passwordHash, now);
    }

    /**
     * Приводит email к каноническому виду для хранения и поиска.
     *
     * @param email исходный email
     * @return email без пробелов по краям в нижнем регистре
     */
    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
