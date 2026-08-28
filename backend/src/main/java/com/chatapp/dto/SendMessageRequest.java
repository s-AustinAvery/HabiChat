package com.chatapp.dto;

import com.chatapp.constants.MessageConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank @Size(max = MessageConstraints.MAX_LENGTH) String text
) {
}
