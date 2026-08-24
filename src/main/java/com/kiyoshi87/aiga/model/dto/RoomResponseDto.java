package com.kiyoshi87.aiga.model.dto;

import lombok.Builder;

@Builder
public record RoomResponseDto(
        Long roomId,
        String shareUrl,
        MediaResponseDto media
) {
}
