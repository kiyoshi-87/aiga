package com.kiyoshi87.aiga.service;

import com.kiyoshi87.aiga.model.RoomStatus;
import com.kiyoshi87.aiga.model.SourceType;
import com.kiyoshi87.aiga.model.dto.CreateRoomRequestDto;
import com.kiyoshi87.aiga.model.dto.RoomResponseDto;
import com.kiyoshi87.aiga.model.entity.Media;
import com.kiyoshi87.aiga.model.entity.Room;
import com.kiyoshi87.aiga.model.entity.User;
import com.kiyoshi87.aiga.repository.MediaRepository;
import com.kiyoshi87.aiga.repository.RoomRepository;
import com.kiyoshi87.aiga.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public RoomResponseDto createRoom(CreateRoomRequestDto request, String userEmail, String applicationUrl) {
        User host = userRepository.findByEmail(normalizeEmail(userEmail))
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
        String sourceUrl = validateMediaUrl(request.sourceUrl());

        Media media = mediaRepository.save(Media.builder()
                .sourceType(SourceType.EXTERNAL_URL)
                .sourceUrl(sourceUrl)
                .build());
        Room room = roomRepository.save(Room.builder()
                .host(host)
                .media(media)
                .status(RoomStatus.CREATED)
                .build());

        return new RoomResponseDto(room.getId(), applicationUrl + "/room/" + room.getId());
    }

    private String validateMediaUrl(String value) {
        try {
            URI uri = new URI(value.trim());
            if ((!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null) {
                throw new IllegalArgumentException("Media URL must be a valid HTTP(S) URL");
            }
            return uri.normalize().toString();
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Media URL must be a valid HTTP(S) URL");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
