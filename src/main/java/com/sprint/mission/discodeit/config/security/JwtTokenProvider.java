package com.sprint.mission.discodeit.config.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.entity.DiscodeitUserDetails;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {
    public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

    /* .env에 명시해둔 토큰 유효 기간 */
    private final int accessTokenExpirationMs;
    /* 헤더,페이로드 Base64인코딩 진행하고, 비밀키로 해시까지 때려줌 그리고 시그니쳐에 입력까지 */
    private final JWSSigner accessTokenSigner;
    /* 비밀키 해쉬때려서 헤더+페이로드가 시그니처와 동일한지 토큰 검사해줌*/
    private final JWSVerifier accessTokenVerifier;

    private final long refreshTokenExpirationMs;
    private final JWSSigner refreshTokenSigner;
    private final JWSVerifier refreshTokenVerifier;

    public JwtTokenProvider(
        @Value("${discodeit.jwt.access-token.secret}") String secret,
        @Value("${discodeit.jwt.access-token.expiration-ms}") int accessTokenExpirationMs,
        @Value("${discodeit.jwt.refresh-token.secret}") String refreshSecret,
        @Value("${discodeit.jwt.refresh-token.expiration-ms}") long refreshTokenExpirationMs
    ) throws JOSEException {
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenSigner = new MACSigner(secretBytes);
        this.accessTokenVerifier = new MACVerifier(secretBytes);

        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        byte[] refreshSecretBytes = refreshSecret.getBytes(StandardCharsets.UTF_8);
        this.refreshTokenSigner = new MACSigner(refreshSecretBytes);
        this.refreshTokenVerifier = new MACVerifier(refreshSecretBytes);
    }

    /* jwt 생성 로직, 여기서 시그니처까지 다 붙히고 온전한 jwt토큰 생성 */
    public String generateAccessToken(DiscodeitUserDetails userDetails) throws JOSEException {
        /* 페이로드에 들어갈 만료시각 구하기 */
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(accessTokenExpirationMs);

        /* claimSet = 페이로드 */
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(userDetails.getUsername())
            .jwtID(UUID.randomUUID().toString())
            .claim("userId", userDetails.getUserDto().id().toString())
            .claim("type", "access")
            .claim("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList())
            .issueTime(Date.from(now))
            .expirationTime(Date.from(expiryDate))
            .build();

        /* jwt토큰 생성및, 헤더 정의(해시 알고리즘 뭐썼는지), 위에서 만든 페이로드 넣기
         * 아직 여기에서는 base64url인코딩도 안되어있고, 시그니처도 안붙어있음  */
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

        /* 시그니처 부분 추가 */
        signedJWT.sign(accessTokenSigner);

        /* 토큰 전체 base64url로 인코딩하고 직렬화 반환(String타입) */
        return signedJWT.serialize();
    }

    /* 토큰 유효한지 검증, 2가지 검증예정
     * 만료시간 지났나?, 헤더랑 페이로드 해쉬때렸을때 시그니처와 동일한가? */
    public boolean validateAccessToken(String token) {
        try {
            /* 얘가 알아서 문자열을 파싱해서 토큰 만들어준다고함 */
            SignedJWT signedJWT = SignedJWT.parse(token);
            /* 만료시간 체크 */
            if (signedJWT.getJWTClaimsSet().getExpirationTime() == null ||
                signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date())) {
                return false;
            }
            /* 헤더랑 페이로드 위변조 체크 */
            if (!signedJWT.verify(accessTokenVerifier))
//                얘네가 내부적으로 돌아감
//                signedJWT.getHeader(),
//                signedJWT.getJWTClaimsSet(),
//                signedJWT.getSignature())
         {
            return false;
         }
            return true;
        } catch (ParseException | JOSEException e) {
            log.debug("Access Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public Date getIssueTimeFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getIssueTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public Date getExpirationTimeFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getExpirationTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    /* refresh 토큰 생성 로직 (최초 발급 시 현재 시각 기준 refreshTokenExpirationMs 후 만료) */
    public String generateRefreshToken(DiscodeitUserDetails userDetails) throws JOSEException {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(refreshTokenExpirationMs);
        return generateRefreshToken(userDetails, Date.from(now), Date.from(expiryDate));
    }

    /* refresh 토큰 재발급 로직 (최초 발급시각·만료시각 고정, Rotation 시 유효기간 연장 방지) */
    public String generateRefreshToken(
        DiscodeitUserDetails userDetails,
        Date issueTime,
        Date expirationTime
    ) throws JOSEException {
        /* claimSet = 페이로드 */
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(userDetails.getUsername())
            .jwtID(UUID.randomUUID().toString())
            .claim("userId", userDetails.getUserDto().id().toString())
            .claim("type", "refresh")
            .claim("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList())
            .issueTime(issueTime)
            .expirationTime(expirationTime)
            .build();

        /* jwt토큰 생성및, 헤더 정의(해시 알고리즘 뭐썼는지), 위에서 만든 페이로드 넣기
         * 아직 여기에서는 base64url인코딩도 안되어있고, 시그니처도 안붙어있음  */
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

        /* 시그니처 부분 추가 */
        signedJWT.sign(refreshTokenSigner);

        /* 토큰 전체 base64url로 인코딩하고 직렬화 반환(String타입) */
        return signedJWT.serialize();
    }

    /* refresh 토큰 유효한지 검증, 2가지 검증예정
     * 만료시간 지났나?, 헤더랑 페이로드 해쉬때렸을때 시그니처와 동일한가? */
    public boolean validateRefreshToken(String token) {
        try {
            /* 얘가 알아서 문자열을 파싱해서 토큰 만들어준다고함 */
            SignedJWT signedJWT = SignedJWT.parse(token);
            /* 만료시간 체크 */
            if (signedJWT.getJWTClaimsSet().getExpirationTime() == null ||
                signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date())) {
                return false;
            }
            /* 헤더랑 페이로드 위변조 체크 */
            if (!signedJWT.verify(refreshTokenVerifier))
//                얘네가 내부적으로 돌아감
//                signedJWT.getHeader(),
//                signedJWT.getJWTClaimsSet(),
//                signedJWT.getSignature())
            {
                return false;
            }
            return true;
        } catch (ParseException | JOSEException e) {
            log.debug("Refresh Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /* refresh Token 쿠키 생성
     * 얘는 쿠키에 위치해야함 프론트 메모리에 위차하면 새로고침마다 초기화
     * Rotation 시에도 쿠키 만료시간이 초기화되지 않도록 토큰의 남은 유효기간으로 Max-Age 설정 */
    public Cookie generateRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        /* 쿠키에 위치하기에 XSS 방지위한 httpOnly 옵션 */
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        Date expirationTime = getExpirationTimeFromToken(refreshToken);
        long remainingSeconds = expirationTime != null
            ? Math.max(0, (expirationTime.getTime() - System.currentTimeMillis()) / 1000)
            : refreshTokenExpirationMs / 1000;
        cookie.setMaxAge((int) remainingSeconds);
        return cookie;
    }

    /* refresh Token 쿠키 만료(삭제용) */
    public Cookie generateRefreshTokenExpirationCookie() {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
