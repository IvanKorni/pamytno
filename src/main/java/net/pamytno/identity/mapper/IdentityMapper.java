package net.pamytno.identity.mapper;

import net.pamytno.identity.domain.User;
import net.pamytno.identity.rest.dto.AuthTokenDto;
import net.pamytno.identity.rest.dto.UserDto;
import net.pamytno.identity.service.AccessToken;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Преобразует пользователя и токен в DTO REST API.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface IdentityMapper {

    /**
     * Профиль пользователя.
     *
     * @param user пользователь
     * @return DTO профиля
     */
    UserDto toDto(User user);

    /**
     * Ответ с токеном.
     *
     * @param token выпущенный токен
     * @return DTO токена с типом {@code Bearer}
     */
    @Mapping(target = "tokenType", constant = "Bearer")
    AuthTokenDto toDto(AccessToken token);
}
