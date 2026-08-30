package com.kiyoshi87.aiga.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomConnectionManager {

    private final Map<Long, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>(); // note we are adding states here

    public void joinRoom(Long roomId, WebSocketSession session) {
        rooms.computeIfAbsent(roomId, id -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    public void leaveRoom(Long roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = rooms.get(roomId);

        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                rooms.remove(roomId);
            }
        }
    }

    public void removeSession(WebSocketSession session) {
        rooms.forEach((roomId, sessions) -> {
            sessions.remove(session);

            if (sessions.isEmpty()) {
                rooms.remove(roomId, sessions);
            }
        });
    }

    public boolean isSessionPartOfRoom(WebSocketSession session, long roomId) {
        return rooms.containsKey(roomId) && rooms.get(roomId).contains(session);
    }
}
