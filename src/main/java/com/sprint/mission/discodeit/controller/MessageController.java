package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentCreateRequestDto;
import com.sprint.mission.discodeit.dto.messagedto.MessageCreateRequestDto;
import com.sprint.mission.discodeit.dto.messagedto.MessageResponseDto;
import com.sprint.mission.discodeit.dto.messagedto.MessageUpdateRequestDto;
import com.sprint.mission.discodeit.mapper.MultipartFileMapper;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;
    private final MultipartFileMapper multipartFileMapper;

    // URL 충돌방지를 위해 consumes로 미디어 타입 명시
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponseDto send(@RequestBody MessageCreateRequestDto dto) {
        return messageService.createMessage(dto);
    }

    // URL 충돌방지를 위해 consumes로 미디어 타입 명시
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponseDto sendWithAttachments(@RequestPart("messageCreateRequest") MessageCreateRequestDto dto,
                                                  @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
        // MultipartFile 리스트 → BinaryContentCreateRequestDto 리스트 변환 후 dto에 연결
        List<BinaryContentCreateRequestDto> attachmentDtos = multipartFileMapper.toDtoList(attachments);
        dto.setAttachmentDtos(attachmentDtos);

        return messageService.createMessage(dto);
    }

    @PatchMapping("/{messageId}")
    public MessageResponseDto update(@PathVariable UUID messageId,
                                     @RequestBody MessageUpdateRequestDto dto) {
        return messageService.updateMessage(messageId, dto);
    }

    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID messageId) {
        messageService.deleteMessage(messageId);
    }

    @GetMapping
    public List<MessageResponseDto> findAll(@RequestParam UUID channelId) {
        return messageService.findAllByChannelId(channelId);
    }
}
