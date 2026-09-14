package com.kiyoshi87.aiga.websocket;

import com.kiyoshi87.aiga.model.dto.websocket.RoomLeaveResult;
import com.kiyoshi87.aiga.model.dto.websocket.RoomParticipant;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomConnectionManager {

    private final Map<Long, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final Map<String, Long> activeRoomsBySessionId = new ConcurrentHashMap<>();

    public void joinRoom(Long roomId, WebSocketSession session) {
        Long activeRoomId = activeRoomsBySessionId.putIfAbsent(session.getId(), roomId);

        if (activeRoomId != null && activeRoomId.longValue() != roomId) {
            throw new IllegalArgumentException("A session can only join one room at a time");
        }

        rooms.computeIfAbsent(roomId, id -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    public boolean leaveRoom(Long roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = rooms.get(roomId);
        activeRoomsBySessionId.remove(session.getId(), roomId);

        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                rooms.remove(roomId);
                return true;
            }
        }

        return false;
    }

    public Optional<RoomLeaveResult> removeSession(WebSocketSession session) {
        Long roomId = activeRoomsBySessionId.remove(session.getId());
        if (roomId == null) {
            return Optional.empty();
        }

        RoomParticipant participant = participantFor(session);
        return Optional.of(RoomLeaveResult.builder()
                .roomId(roomId)
                .roomEmpty(leaveRoom(roomId, session))
                .participant(participant)
                .build());
    }

    public boolean isSessionPartOfRoom(WebSocketSession session, long roomId) {
        return activeRoomsBySessionId.getOrDefault(session.getId(), -1L) == roomId;
    }

    public long requireActiveRoomId(WebSocketSession session) {
        Long roomId = activeRoomsBySessionId.get(session.getId());
        if (roomId == null) {
            throw new IllegalArgumentException("Join a room before sending playback commands");
        }
        return roomId;
    }

    public Set<WebSocketSession> sessionsInRoom(long roomId) {
        return rooms.getOrDefault(roomId, Set.of());
    }

    public RoomParticipant participantFor(WebSocketSession session) {
        Object userId = session.getAttributes().get(AigaHandshakeInterceptor.USER_ID_ATTRIBUTE);
        String participantId = session.getId();
        Long lUserId = userId instanceof Long id ? id : null;

        return RoomParticipant.builder()
                .participantId(participantId)
                .userId(lUserId)
                .build();
    }

    public List<RoomParticipant> participantsInRoom(long roomId) {
        return sessionsInRoom(roomId).stream()
                .map(this::participantFor)
                .sorted(Comparator.comparing(RoomParticipant::participantId))
                .toList();
    }
}
