package net.pamytno.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.pamytno.common.error.ErrorCodes;
import net.pamytno.common.error.ErrorResponseFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Пишет ошибки Spring Security (401/403) в едином формате {@code ErrorResponse}.
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorWriter implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ErrorResponseFactory errors;
    private final ObjectMapper objectMapper;

    /**
     * Запрос без валидного токена — 401.
     *
     * @param request       HTTP-запрос
     * @param response      HTTP-ответ
     * @param authException причина отказа
     * @throws IOException если не удалось записать ответ
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Требуется аутентификация");
    }

    /**
     * Недостаточно прав — 403.
     *
     * @param request               HTTP-запрос
     * @param response              HTTP-ответ
     * @param accessDeniedException причина отказа
     * @throws IOException если не удалось записать ответ
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        write(response, HttpStatus.FORBIDDEN, ErrorCodes.ACCESS_DENIED, "Доступ запрещён");
    }

    /**
     * Записывает тело ошибки в ответ.
     *
     * @param response HTTP-ответ
     * @param status   статус
     * @param code     код ошибки
     * @param message  сообщение
     * @throws IOException если не удалось записать ответ
     */
    private void write(HttpServletResponse response, HttpStatus status, String code, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), errors.create(code, message));
    }
}
