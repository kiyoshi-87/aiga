package com.kiyoshi87.aiga.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MessageType {
    JOIN_ROOM("JOIN_ROOM"),
    LEAVE_ROOM("LEAVE_ROOM");

    final String type;

    MessageType(String type) {
        this.type = type;
    }

    public boolean equalsIgnoreCase(String messageType) {
        return this.type.equalsIgnoreCase(messageType);
    }

    public static boolean isValidType(String type) {
        if (type == null) {
            return false;
        }

        return Arrays.stream(MessageType.values())
                .anyMatch(messageType -> messageType.equalsIgnoreCase(type.trim()));
    }
}
