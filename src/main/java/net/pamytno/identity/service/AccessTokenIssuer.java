package net.pamytno.identity.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.JwtProperties;
import net.pamytno.identity.domain.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;

/**
 * Выпускает JWT для пользователя: {@code sub} — идентификатор, {@code email} — для удобства фронтенда.
 */
@Service
@RequiredArgsConstructor
public class AccessTokenIssuer {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final Clock clock;

    /**
     * Выпускает токен.
     *
     * @param user владелец токена
     * @return токен и его срок жизни
     */
    public AccessToken issue(User user) {
        var now = clock.instant();
        var claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(properties.ttl()))
                .claim("email", user.getEmail())
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        var token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AccessToken(token, properties.ttl().toSeconds(), user.getId());
    }
}
