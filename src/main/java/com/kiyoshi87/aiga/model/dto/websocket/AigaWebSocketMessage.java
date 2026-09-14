package com.kiyoshi87.aiga.model.dto.websocket;

import tools.jackson.databind.JsonNode;

public record AigaWebSocketMessage(
        String type,
        String requestId,
        JsonNode payload // JsonNode for now, cause there will be many different payloads depending on the message type
) {
}
