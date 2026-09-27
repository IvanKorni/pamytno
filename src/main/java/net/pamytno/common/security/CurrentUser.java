package net.pamytno.common.security;

import net.pamytno.common.error.ErrorCodes;
import net.pamytno.common.error.UnauthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Даёт идентификатор текущего пользователя из JWT (claim {@code sub}).
 * Единственный способ для модулей узнать, кто выполняет запрос.
 */
@Component
public class CurrentUser {

    /**
     * Возвращает идентификатор аутентифицированного пользователя.
     *
     * @return идентификатор пользователя
     * @throws UnauthorizedException если запрос не аутентифицирован JWT
     */
    public UUID id() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return UUID.fromString(token.getToken().getSubject());
        }
        throw new UnauthorizedException(ErrorCodes.UNAUTHORIZED, "Требуется аутентификация");
    }
}
