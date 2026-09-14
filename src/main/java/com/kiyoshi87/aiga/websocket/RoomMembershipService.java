package com.kiyoshi87.aiga.websocket;

import com.kiyoshi87.aiga.model.dto.websocket.RoomLeaveResult;
import com.kiyoshi87.aiga.model.dto.websocket.RoomParticipant;
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
        long roomId = getAndValidateRoomId(payload);
        validateRoomExists(roomId);

        roomConnectionManager.joinRoom(roomId, session);
        log.info("Session {} joined room {}", session.getId(), roomId);

        return roomId;
    }

    public RoomLeaveResult leave(JsonNode payload, WebSocketSession session) {
        long roomId = getAndValidateRoomId(payload);
        validateRoomExists(roomId);

        if (!roomConnectionManager.isSessionPartOfRoom(session, roomId)) {
            throw new IllegalArgumentException("Cannot leave room " + roomId + " as you are not a member");
        }

        RoomParticipant participant = roomConnectionManager.participantFor(session);

        boolean roomEmpty = roomConnectionManager.leaveRoom(roomId, session);
        log.info("Session {} left room {}", session.getId(), roomId);

        return RoomLeaveResult.builder()
                .roomId(roomId)
                .roomEmpty(roomEmpty)
                .participant(participant)
                .build();
    }

    private long getAndValidateRoomId(JsonNode payload) {
        if (payload == null || !payload.has("roomId")) {
            throw new IllegalArgumentException("payload.roomId is required");
        }

        JsonNode roomIdNode = payload.get("roomId");
        if (!roomIdNode.isIntegralNumber() || roomIdNode.asLong() <= 0) {
            throw new IllegalArgumentException("payload.roomId must be a positive integer");
        }

        return roomIdNode.asLong();
    }

    private void validateRoomExists(long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new IllegalArgumentException("Room with ID " + roomId + " does not exist");
        }
    }
}
