package net.pamytno.identity.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.identity.mapper.IdentityMapper;
import net.pamytno.identity.rest.api.UsersApi;
import net.pamytno.identity.rest.dto.UserDto;
import net.pamytno.identity.service.UserQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Профиль текущего пользователя ({@code /api/users/me}).
 */
@RestController
@RequiredArgsConstructor
public class UserRestControllerV1 implements UsersApi {

    private final UserQueryService userQueryService;
    private final CurrentUser currentUser;
    private final IdentityMapper identityMapper;

    /**
     * Возвращает профиль пользователя из токена.
     *
     * @return 200 и профиль
     */
    @Override
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(identityMapper.toDto(userQueryService.getById(currentUser.id())));
    }
}
