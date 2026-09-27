package net.pamytno.identity.service;

import java.util.UUID;

/**
 * Выпущенный access-токен.
 *
 * @param accessToken подписанный JWT
 * @param expiresIn   время жизни в секундах
 * @param userId      владелец токена
 */
public record AccessToken(String accessToken, long expiresIn, UUID userId) {
}
