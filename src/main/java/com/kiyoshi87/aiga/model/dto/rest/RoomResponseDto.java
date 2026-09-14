package com.kiyoshi87.aiga.model.dto.rest;

import lombok.Builder;

@Builder
public record RoomResponseDto(
        Long roomId,
        String shareUrl,
        Long hostUserId,
        MediaResponseDto media
) {
}
