package com.sprint.mission.discodeit.dto.authdto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthCreateRequestDto {
    String username;
    String password;
}
