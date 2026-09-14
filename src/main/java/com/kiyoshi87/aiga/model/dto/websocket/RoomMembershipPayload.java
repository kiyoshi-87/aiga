package com.kiyoshi87.aiga.model.dto.websocket;

import lombok.Builder;

import java.util.List;

/** Payload returned after a participant joins or leaves a room. */
@Builder
public record RoomMembershipPayload(
        long roomId,
        String participantId,
        PlaybackState playback,
        List<RoomParticipant> participants
) {
}
