package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatusdto.ReadStatusCreateRequestDto;
import com.sprint.mission.discodeit.dto.readstatusdto.ReadStatusResponseDto;
import com.sprint.mission.discodeit.dto.readstatusdto.ReadStatusUpdateRequestDto;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/readStatuses")
public class ReadStatusController {
    private final ReadStatusService readStatusService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReadStatusResponseDto create(@RequestBody ReadStatusCreateRequestDto dto) {
        return readStatusService.createReadStatus(dto);
    }

    @PatchMapping("/{readStatusId}")
    public ReadStatusResponseDto update(@PathVariable UUID readStatusId,
                                        @RequestBody ReadStatusUpdateRequestDto dto) {
        return readStatusService.updateReadStatus(readStatusId, dto);
    }

    @GetMapping
    public List<ReadStatusResponseDto> findAll(@RequestParam UUID userId) {
        return readStatusService.findAllByUserId(userId);
    }
}
