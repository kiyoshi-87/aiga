package com.kiyoshi87.aiga.websocket;

import com.kiyoshi87.aiga.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.JsonNode;

/** Coordinates validation and connection tracking for room membership changes. */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoomMembershipService {

    private final RoomRepository roomRepository;
    private final RoomConnectionManager roomConnectionManager;

    public long join(JsonNode payload, WebSocketSession session) {
        long roomId = requiredRoomId(payload);
        requireExistingRoom(roomId);

        roomConnectionManager.joinRoom(roomId, session);
        log.info("Session {} joined room {}", session.getId(), roomId);

        return roomId;
    }

    public long leave(JsonNode payload, WebSocketSession session) {
        long roomId = requiredRoomId(payload);
        requireExistingRoom(roomId);

        if (!roomConnectionManager.isSessionPartOfRoom(session, roomId)) {
            throw new IllegalArgumentException("Cannot leave room " + roomId + " as you are not a member");
        }

        roomConnectionManager.leaveRoom(roomId, session);
        log.info("Session {} left room {}", session.getId(), roomId);
        return roomId;
    }

    private long requiredRoomId(JsonNode payload) {
        if (payload == null || !payload.has("roomId")) {
            throw new IllegalArgumentException("payload.roomId is required");
        }

        JsonNode roomIdNode = payload.get("roomId");
        if (!roomIdNode.isIntegralNumber() || roomIdNode.asLong() <= 0) {
            throw new IllegalArgumentException("payload.roomId must be a positive integer");
        }

        return roomIdNode.asLong();
    }

    private void requireExistingRoom(long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new IllegalArgumentException("Room with ID " + roomId + " does not exist");
        }
    }
}
