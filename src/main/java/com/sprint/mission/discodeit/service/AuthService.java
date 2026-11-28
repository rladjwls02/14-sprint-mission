package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.data.JwtInformation;

public interface AuthService {
    UserDto updateRole(UserRoleUpdateRequest request);
    JwtInformation refreshToken(String refreshToken);
}
