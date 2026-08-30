package com.kiyoshi87.aiga.model.dto;

/** An error response sent over the Aiga WebSocket protocol. */
public record WebSocketErrorResponse(
        String status,
        String type,
        String requestId,
        WebSocketError error
) {
    public static WebSocketErrorResponse of(String requestId, String code, String message) {
        return new WebSocketErrorResponse("error", "ERROR", requestId, new WebSocketError(code, message));
    }
}
