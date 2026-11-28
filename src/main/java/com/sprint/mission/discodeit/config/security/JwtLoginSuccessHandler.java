package com.sprint.mission.discodeit.config.security;

import com.nimbusds.jose.JOSEException;
import com.sprint.mission.discodeit.dto.data.JwtDto;
import com.sprint.mission.discodeit.dto.data.JwtInformation;
import com.sprint.mission.discodeit.entity.DiscodeitUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {
        DiscodeitUserDetails userDetails = (DiscodeitUserDetails) authentication.getPrincipal();

        try {
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            /* 레지스트리에 등록 (최대 동시 로그인 수 1 제한 -> 기존 세션 무효화) */
            JwtInformation jwtInformation = new JwtInformation(
                userDetails.getUserDto(),
                accessToken,
                refreshToken
            );
            jwtRegistry.registerJwtInformation(jwtInformation);

            /* 리프레시토큰이랑 액세스토큰 동시생성해야됌
             * 리프레시토큰 먼저 생성  */
            Cookie refreshCookie = jwtTokenProvider.generateRefreshTokenCookie(refreshToken);
            response.addCookie(refreshCookie);

            JwtDto jwtDto = new JwtDto(userDetails.getUserDto(), accessToken);

            log.info("로그인 성공 (200) - username: {}", userDetails.getUsername());

            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(jwtDto));
        } catch (JOSEException e) {
            log.error("JWT 토큰 생성 실패", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
