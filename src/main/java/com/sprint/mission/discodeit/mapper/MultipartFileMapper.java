package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentCreateRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class MultipartFileMapper {

    // 단건 변환 (유저 프로필용)
    public BinaryContentCreateRequestDto toDto(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        return new BinaryContentCreateRequestDto(
                file.getOriginalFilename(), null, null
        );
    }

    // 다건 변환 (메세지 첨부파일용)
    public List<BinaryContentCreateRequestDto> toDtoList(List<MultipartFile> files) {
        if (files == null) return List.of();
        return files.stream()
                .filter(each -> each != null && !each.isEmpty())
                .map(this::toDto)
                .toList();
    }
}
