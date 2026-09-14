package com.kiyoshi87.aiga.model.dto.websocket;

import lombok.Builder;

@Builder
public record RoomLeaveResult(
        long roomId,
        boolean roomEmpty,
        RoomParticipant participant) {
}
