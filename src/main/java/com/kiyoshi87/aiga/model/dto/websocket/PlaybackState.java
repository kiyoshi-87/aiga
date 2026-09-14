package com.kiyoshi87.aiga.model.dto.websocket;

import com.kiyoshi87.aiga.model.PlaybackStatus;
import lombok.Builder;

@Builder
public record PlaybackState(
        PlaybackStatus status,
        double position,
        long serverTimestamp
) {
}
