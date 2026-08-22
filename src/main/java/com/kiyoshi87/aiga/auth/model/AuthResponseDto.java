package com.kiyoshi87.aiga.auth.model;

import lombok.Builder;

@Builder
public record AuthResponseDto(
    Long id,
    String email
) { }
