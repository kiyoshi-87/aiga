package com.kiyoshi87.aiga.model.dto;

/** A successful response sent over the Aiga WebSocket protocol. */
public record WebSocketSuccessResponse<T>(
        String status,
        String type,
        String requestId,
        T payload
) {
    public static <T> WebSocketSuccessResponse<T> of(String type, String requestId, T payload) {
        return new WebSocketSuccessResponse<>("success", type, requestId, payload);
    }
}
