package com.kiyoshi87.aiga.model.dto.websocket;

import lombok.Builder;

@Builder
public record RoomPresencePayload(
        long roomId,
        RoomParticipant participant
) {
}
