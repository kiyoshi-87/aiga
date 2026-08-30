package com.kiyoshi87.aiga.model.dto;

import lombok.Builder;

/** Payload returned after a participant joins or leaves a room. */
@Builder
public record RoomMembershipPayload(
        long roomId,
        String participantId
) {
}
