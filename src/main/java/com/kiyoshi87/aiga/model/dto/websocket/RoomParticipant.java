package com.kiyoshi87.aiga.model.dto.websocket;

import lombok.Builder;

@Builder
public record RoomParticipant(
        String participantId,
        Long userId
) {
}
