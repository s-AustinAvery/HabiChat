package com.chatapp.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID roomId,
        String senderUsername,
        String text,
        Instant sentAt
) {
}
