package com.kiyoshi87.aiga.websocket;

import com.kiyoshi87.aiga.model.MessageType;
import com.kiyoshi87.aiga.model.dto.AigaWebSocketMessage;
import com.kiyoshi87.aiga.model.dto.RoomMembershipPayload;
import com.kiyoshi87.aiga.model.dto.WebSocketErrorResponse;
import com.kiyoshi87.aiga.model.dto.WebSocketSuccessResponse;
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


@Component
@Slf4j
@RequiredArgsConstructor
public class AigaWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final RoomConnectionManager roomConnectionManager;
    private final RoomMembershipService roomMembershipService;

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
                case "JOIN_ROOM" -> handleJoinRoom(request, session);
                case "LEAVE_ROOM" -> handleLeaveRoom(request, session);
                default -> sendError(session, requestId, "UNKNOWN_MESSAGE_TYPE",
                        "Unsupported message type: " + request.type());
            }
        } catch (JacksonException exception) {
            log.debug("Invalid WebSocket JSON from session {}", session.getId(), exception);
            sendError(session, requestId, "INVALID_JSON", "Message must be valid JSON.");
        } catch (IllegalArgumentException exception) {
            sendError(session, requestId, "INVALID_REQUEST", exception.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        roomConnectionManager.removeSession(session);
        log.info("Disconnected: {}", session.getId());
    }

    private void handleJoinRoom(AigaWebSocketMessage request, WebSocketSession session) throws IOException {
        long roomId = roomMembershipService.join(request.payload(), session);

        sendSuccess(session, "ROOM_JOINED", request.requestId(),
                RoomMembershipPayload.builder()
                        .roomId(roomId)
                .build());
    }

    private void handleLeaveRoom(AigaWebSocketMessage request, WebSocketSession session) throws IOException {
        long roomId = roomMembershipService.leave(request.payload(), session);

        sendSuccess(session, "ROOM_LEFT", request.requestId(),
                RoomMembershipPayload.builder()
                .roomId(roomId)
                .build());
    }

    private String requireMessageType(AigaWebSocketMessage request) {
        String type = request.type();

        if (StringUtils.hasText(type)) {
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
}
