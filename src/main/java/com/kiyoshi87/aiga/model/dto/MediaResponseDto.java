package com.kiyoshi87.aiga.model.dto;

import lombok.Builder;

@Builder
public record MediaResponseDto(
        String sourceType,
        String sourceUrl
) {
}
