package com.sprint.mission.discodeit.dto.messagedto;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentCreateRequestDto;
import com.sprint.mission.discodeit.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateRequestDto {
    private String content;
    private UUID channelId;
    private UUID authorId;
    private List<BinaryContentCreateRequestDto> attachmentDtos;

    public MessageCreateRequestDto(String content, UUID channelId, UUID authorId) {
        this(content, channelId, authorId, null);
    }

    public Message toEntity() {
        return new Message(this.content, this.channelId, this.authorId);
    }
}
