package net.pamytno.identity.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.identity.mapper.IdentityMapper;
import net.pamytno.identity.rest.api.AuthApi;
import net.pamytno.identity.rest.dto.AuthTokenDto;
import net.pamytno.identity.rest.dto.LoginRequest;
import net.pamytno.identity.rest.dto.RegisterRequest;
import net.pamytno.identity.service.LoginService;
import net.pamytno.identity.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Регистрация и вход ({@code /api/auth}).
 */
@RestController
@RequiredArgsConstructor
public class AuthRestControllerV1 implements AuthApi {

    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final IdentityMapper identityMapper;

    /**
     * Регистрирует пользователя и возвращает токен.
     *
     * @param request email и пароль
     * @return 201 и токен
     */
    @Override
    public ResponseEntity<AuthTokenDto> register(RegisterRequest request) {
        var token = registrationService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(identityMapper.toDto(token));
    }

    /**
     * Выполняет вход и возвращает токен.
     *
     * @param request email и пароль
     * @return 200 и токен
     */
    @Override
    public ResponseEntity<AuthTokenDto> login(LoginRequest request) {
        var token = loginService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(identityMapper.toDto(token));
    }
}
