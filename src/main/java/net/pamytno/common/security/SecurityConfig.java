package net.pamytno.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Цепочка фильтров безопасности: stateless JWT, открытые эндпоинты аутентификации и документации,
 * остальное — только для аутентифицированных.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
        "/api/auth/**",
        "/actuator/health/**",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    private static final long CORS_MAX_AGE_SECONDS = 3600;

    private final SecurityErrorWriter securityErrorWriter;

    /**
     * Основная цепочка фильтров.
     *
     * @param http построитель Spring Security
     * @param cors настройки CORS
     * @return цепочка фильтров
     * @throws Exception если конфигурация некорректна
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsProperties cors) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(configurer -> configurer.configurationSource(corsConfigurationSource(cors)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(securityErrorWriter))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(securityErrorWriter)
                        .accessDeniedHandler(securityErrorWriter))
                .build();
    }

    /**
     * CORS для фронтенда.
     *
     * @param cors настройки CORS
     * @return источник CORS-конфигурации
     */
    private CorsConfigurationSource corsConfigurationSource(CorsProperties cors) {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(cors.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(CORS_MAX_AGE_SECONDS);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
