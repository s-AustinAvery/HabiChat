package com.habichat.dto;

import java.time.Instant;
import java.util.UUID;

public record RoomResponse(
        UUID id,
        String name,
        boolean isDefault,
        Instant createdAt,
        Instant expiresAt,
        int occupantCount
) {
}
