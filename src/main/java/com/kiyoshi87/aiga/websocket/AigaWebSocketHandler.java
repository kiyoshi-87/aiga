package com.kiyoshi87.aiga.websocket;

import com.kiyoshi87.aiga.model.MessageType;
import com.kiyoshi87.aiga.model.dto.websocket.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Locale;

import static com.kiyoshi87.aiga.util.AigaConstants.*;


@Component
@Slf4j
@RequiredArgsConstructor
public class AigaWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final RoomConnectionManager roomConnectionManager;
    private final RoomMembershipService roomMembershipService;
    private final PlaybackStateManager playbackStateManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Connected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws IOException {
        String requestId = null;

        try {
            AigaWebSocketMessage request = objectMapper.readValue(message.getPayload(), AigaWebSocketMessage.class);
            requestId = request.requestId();

            // This can be extracted or handled better when we will have additional message types
            switch (requireMessageType(request)) {
                case JOIN_ROOM -> handleJoinRoom(request, session);
                case LEAVE_ROOM -> handleLeaveRoom(request, session);
                case PLAY -> handlePlaybackCommand(request, session, PLAY);
                case PAUSE -> handlePlaybackCommand(request, session, PAUSE);
                case SEEK -> handlePlaybackCommand(request, session, SEEK);
                default -> sendError(session, requestId, UNKNOWN_MESSAGE_TYPE,
                        "Unsupported message type: " + request.type());
            }
        } catch (JacksonException exception) {
            log.debug("Invalid WebSocket JSON from session {}", session.getId(), exception);
            sendError(session, requestId, INVALID_JSON, "Message must be valid JSON.");
        } catch (IllegalArgumentException exception) {
            sendError(session, requestId, INVALID_REQUEST, exception.getMessage());
        } catch (PlaybackAuthorizationException exception) {
            sendError(session, requestId, FORBIDDEN, exception.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        roomConnectionManager.removeSession(session).ifPresent(result -> {
            broadcast(result.roomId(), WebSocketSuccessResponse.of(PARTICIPANT_LEFT, null,
                    RoomPresencePayload.builder()
                            .roomId(result.roomId())
                            .participant(result.participant())
                            .build()));
            if (result.roomEmpty()) {
                playbackStateManager.removeState(result.roomId());
            }
        });
        log.info("Disconnected: {}", session.getId());
    }

    private void handleJoinRoom(AigaWebSocketMessage request, WebSocketSession session) throws IOException {
        long roomId = roomMembershipService.join(request.payload(), session);

        sendSuccess(session, ROOM_JOINED, request.requestId(),
                RoomMembershipPayload.builder()
                        .roomId(roomId)
                        .participantId(roomConnectionManager.participantFor(session).participantId())
                        .playback(playbackStateManager.currentState(roomId))
                        .participants(roomConnectionManager.participantsInRoom(roomId))
                .build());

        broadcastExcept(roomId, session, WebSocketSuccessResponse.of(PARTICIPANT_JOINED, null,
                RoomPresencePayload.builder()
                        .roomId(roomId)
                        .participant(roomConnectionManager.participantFor(session))
                        .build()));
    }

    private void handleLeaveRoom(AigaWebSocketMessage request, WebSocketSession session) throws IOException {
        RoomLeaveResult result = roomMembershipService.leave(request.payload(), session);

        sendSuccess(session, ROOM_LEFT, request.requestId(),
                RoomMembershipPayload.builder()
                .roomId(result.roomId())
                .build());

        if (result.roomEmpty()) {
            playbackStateManager.removeState(result.roomId());
        }

        broadcast(result.roomId(), WebSocketSuccessResponse.of(PARTICIPANT_LEFT, null,
                RoomPresencePayload.builder()
                        .roomId(result.roomId())
                        .participant(result.participant())
                        .build()));
    }

    private void handlePlaybackCommand(AigaWebSocketMessage request, WebSocketSession session, String command) {
        long roomId = roomConnectionManager.requireActiveRoomId(session);
        long userId = requireUserId(session);

        Object playbackState = switch (command) {
            case PLAY -> playbackStateManager.play(roomId, userId, request.payload());
            case PAUSE -> playbackStateManager.pause(roomId, userId, request.payload());
            case SEEK -> playbackStateManager.seek(roomId, userId, request.payload());
            default -> throw new IllegalArgumentException("Unsupported playback command: " + command);
        };

        broadcast(roomId, WebSocketSuccessResponse.of(PLAYBACK_UPDATED, null, playbackState));
    }

    private String requireMessageType(AigaWebSocketMessage request) {
        String type = request.type();

        if (!StringUtils.hasText(type)) {
            throw new IllegalArgumentException("Type is required");
        }

        if (!MessageType.isValidType(type)) {
            throw new IllegalArgumentException("Invalid type: " + type);
        }

        return request.type().toUpperCase(Locale.ROOT);
    }

    private void sendSuccess(WebSocketSession session, String type, String requestId, Object payload) throws IOException {
        send(session, WebSocketSuccessResponse.of(type, requestId, payload));
    }

    private void sendError(WebSocketSession session, String requestId, String code, String message) {
        try {
            send(session, WebSocketErrorResponse.of(requestId, code, message));
        } catch (IOException exception) {
            log.warn("Could not send WebSocket error response to session {}", session.getId(), exception);
        }
    }

    private void send(WebSocketSession session, Object response) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private long requireUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get(AigaHandshakeInterceptor.USER_ID_ATTRIBUTE);

        if (!(userId instanceof Long id)) {
            throw new PlaybackAuthorizationException("Only the room host can control playback");
        }

        return id;
    }

    private void broadcast(long roomId, Object response) {
        broadcastExcept(roomId, null, response);
    }

    private void broadcastExcept(long roomId, WebSocketSession excludedSession, Object response) {
        String responseJson;
        try {
            responseJson = objectMapper.writeValueAsString(response);
        } catch (JacksonException exception) {
            log.error("Could not serialize WebSocket broadcast for room {}", roomId, exception);
            return;
        }

        for (WebSocketSession roomSession : roomConnectionManager.sessionsInRoom(roomId)) {
            if (roomSession == excludedSession || !roomSession.isOpen()) {
                continue;
            }
            try {
                roomSession.sendMessage(new TextMessage(responseJson));
            } catch (IOException exception) {
                log.warn("Could not send playback update to session {}", roomSession.getId(), exception);
            }
        }
    }
}
