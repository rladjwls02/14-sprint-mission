package com.sprint.mission.discodeit.dto.channeldto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelUpdateRequestDto {
    // OpenAPI에서 요청을 new로 보내니까 dto의 필드도 new로 변경
    private String newName;
    private String newDescription;
}
