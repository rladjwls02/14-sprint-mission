package com.sprint.mission.discodeit.config.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
        @Nullable Authentication authentication) {
        Cookie refreshTokenExpirationCookie = jwtTokenProvider.generateRefreshTokenExpirationCookie();
        response.addCookie(refreshTokenExpirationCookie);

        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
                .findFirst()
                .ifPresent(cookie -> {
                    String refreshToken = cookie.getValue();
                    if (jwtTokenProvider.validateRefreshToken(refreshToken)) {
                        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
                        jwtRegistry.invalidateJwtInformationByUserId(userId);
                    }
                });
        }
        log.debug("리프레시 토큰 쿠키 삭제 및 레지스트리 무효화 완료");
    }
}
