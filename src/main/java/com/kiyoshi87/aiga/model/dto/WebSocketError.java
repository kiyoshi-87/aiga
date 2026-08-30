package com.kiyoshi87.aiga.model.dto;

/** Machine-readable details for an unsuccessful WebSocket request. */
public record WebSocketError(
        String code,
        String message
) {
}
