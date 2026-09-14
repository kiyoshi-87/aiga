package com.kiyoshi87.aiga.websocket;

import com.kiyoshi87.aiga.model.PlaybackStatus;
import com.kiyoshi87.aiga.model.dto.websocket.PlaybackState;
import com.kiyoshi87.aiga.model.entity.Room;
import com.kiyoshi87.aiga.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PlaybackStateManager {

    private final RoomRepository roomRepository;
    private final Map<Long, PlaybackState> playbackStates = new ConcurrentHashMap<>();

    public PlaybackState currentState(long roomId) {
        return playbackStates.computeIfAbsent(roomId, ignored -> initialState());
    }

    public PlaybackState play(long roomId, long userId, JsonNode payload) {
        return update(roomId, userId, payload, PlaybackStatus.PLAYING);
    }

    public PlaybackState pause(long roomId, long userId, JsonNode payload) {
        return update(roomId, userId, payload, PlaybackStatus.PAUSED);
    }

    public PlaybackState seek(long roomId, long userId, JsonNode payload) {
        double position = requiredPosition(payload);
        authorizeHost(roomId, userId);

        return playbackStates.compute(roomId, (ignored, current) -> new PlaybackState(
                current == null ? PlaybackStatus.PAUSED : current.status(),
                position,
                System.currentTimeMillis()));
    }

    public void removeState(long roomId) {
        playbackStates.remove(roomId);
    }

    private PlaybackState update(long roomId, long userId, JsonNode payload, PlaybackStatus status) {
        double position = requiredPosition(payload);
        authorizeHost(roomId, userId);

        return playbackStates.compute(roomId, (ignored, current) -> PlaybackState.builder()
                .status(status)
                .position(position)
                .serverTimestamp(System.currentTimeMillis())
                .build());
    }

    private void authorizeHost(long roomId, long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + roomId + " does not exist"));

        if (!room.getHost().getId().equals(userId)) {
            throw new PlaybackAuthorizationException("Only the room host can control playback");
        }
    }

    private double requiredPosition(JsonNode payload) {
        if (payload == null || !payload.has("position") || !payload.get("position").isNumber()) {
            throw new IllegalArgumentException("payload.position must be a number");
        }

        double position = payload.get("position").asDouble();

        if (!Double.isFinite(position) || position < 0) {
            throw new IllegalArgumentException("payload.position must be a finite, non-negative number");
        }

        return position;
    }

    private PlaybackState initialState() {
        return new PlaybackState(PlaybackStatus.PAUSED, 0, System.currentTimeMillis());
    }
}
