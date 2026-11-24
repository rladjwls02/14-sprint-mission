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
    /* .env에 명시해둔 토큰 유효 기간 */
    private final int accessTokenExpirationMs;
    /* 헤더,페이로드 Base64인코딩 진행하고, 비밀키로 해시까지 때려줌 그리고 시그니쳐에 입력까지 */
    private final JWSSigner accessTokenSigner;
    /* 비밀키 해쉬때려서 헤더+페이로드가 시그니처와 동일한지 토큰 검사해줌*/
    private final JWSVerifier accessTokenVerifier;

    public JwtTokenProvider(
        @Value("${discodeit.jwt.access-token.secret}") String secret,
        @Value("${discodeit.jwt.access-token.expiration-ms}") int accessTokenExpirationMs
    ) throws JOSEException {
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenSigner = new MACSigner(secretBytes);
        this.accessTokenVerifier = new MACVerifier(secretBytes);
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
}
