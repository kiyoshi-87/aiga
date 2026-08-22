package com.kiyoshi87.aiga.controller;

import com.kiyoshi87.aiga.model.dto.CreateRoomRequestDto;
import com.kiyoshi87.aiga.model.dto.RoomResponseDto;
import com.kiyoshi87.aiga.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponseDto> createRoom(@Valid @RequestBody CreateRoomRequestDto request,
                                                       @AuthenticationPrincipal UserDetails user) {
        String applicationUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        RoomResponseDto response = roomService.createRoom(request, user.getUsername(), applicationUrl);
        return ResponseEntity.status(201).body(response);
    }
}
