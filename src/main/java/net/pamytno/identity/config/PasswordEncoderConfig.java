package net.pamytno.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Хеширование паролей через BCrypt.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Кодировщик паролей.
     *
     * @return BCrypt-кодировщик
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
