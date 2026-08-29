package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentResponseDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binaryContents")
public class BinaryContentController {
    private final BinaryContentService binaryContentService;

    @GetMapping("/{binaryContentId}")
    public BinaryContentResponseDto findById(@PathVariable UUID binaryContentId) {
        return binaryContentService.readBinaryContent(binaryContentId);
    }

    @GetMapping
    public List<BinaryContentResponseDto> findAll(@RequestParam List<UUID> binaryContentIds) {
        return binaryContentService.findAllByIdIn(binaryContentIds);
    }
}
