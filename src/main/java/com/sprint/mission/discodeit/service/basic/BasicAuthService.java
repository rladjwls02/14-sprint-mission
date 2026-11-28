package com.sprint.mission.discodeit.service.basic;

import java.util.Date;
import java.util.Map;
import com.nimbusds.jose.JOSEException;
import com.sprint.mission.discodeit.common.ErrorCode;
import com.sprint.mission.discodeit.common.exception.auth.AuthException;
import com.sprint.mission.discodeit.common.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.config.security.JwtTokenProvider;
import com.sprint.mission.discodeit.dto.data.JwtInformation;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.DiscodeitUserDetails;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;


  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateRole(UserRoleUpdateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));

    user.updateRole(request.newRole());
    return userMapper.toDto(user, null);
  }

  @Override
  public JwtInformation refreshToken(String refreshToken) {
    /* 토큰이 비어있거나, 헤더랑 페이로드가 변조되지 않았는지 */
    if (refreshToken == null || !jwtTokenProvider.validateRefreshToken(refreshToken)) {
      throw new AuthException(ErrorCode.INVALID_TOKEN, Map.of());
    }
    /* 토큰에서 유저 식별값 가져오기 */
    String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) userDetailsService.loadUserByUsername(
        username);

    /* 액세스 토큰, 리프레시 토큰 함께 재발급
     * 근데 rotation방식을 사용하면 리프레시 토큰도 유효기간이 계속 초기화됌
     * 처음 설정한 리프레시토큰 유효기간이 의미가 없게되버림,
     * 그래서 리프레시 토큰 발급시간이랑 만료시간을 추출,
     * 재발급시 위에서 추출한 리프레시 토큰 발행시각, 만료시간 사용해서 토큰유효기간 고정 */
    Date originalIssueTime = jwtTokenProvider.getIssueTimeFromToken(refreshToken);
    Date originalExpirationTime = jwtTokenProvider.getExpirationTimeFromToken(refreshToken);
    try {
      String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
      String newRefreshToken = jwtTokenProvider.generateRefreshToken(
          userDetails, originalIssueTime, originalExpirationTime);
      return new JwtInformation(userDetails.getUserDto(), newAccessToken, newRefreshToken);
    } catch (JOSEException e) {
      throw new AuthException(ErrorCode.INVALID_TOKEN, Map.of());
    }
  }
}
