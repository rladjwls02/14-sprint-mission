package com.sprint.mission.discodeit.dto.data;

public record JwtInformation(
    UserDto userDto,
    String accessToken,
    String refreshToken
) {}
