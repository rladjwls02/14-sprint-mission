package com.sprint.mission.discodeit.dto.userdto;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentCreateRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {
    private String newUsername;
    private String newEmail;
    private String newPassword;
    private BinaryContentCreateRequestDto profileImageRequestDto;

}
