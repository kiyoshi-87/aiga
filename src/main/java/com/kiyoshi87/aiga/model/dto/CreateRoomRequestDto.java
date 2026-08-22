package com.kiyoshi87.aiga.model.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRoomRequestDto(
        @NotBlank
        String sourceUrl
) {
}
