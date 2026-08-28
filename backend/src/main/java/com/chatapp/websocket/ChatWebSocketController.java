package com.chatapp.websocket;

import com.chatapp.dto.SendMessageRequest;
import com.chatapp.entity.Identity;
import com.chatapp.entity.Room;
import com.chatapp.service.IdentityService;
import com.chatapp.service.MessageService;
import com.chatapp.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class ChatWebSocketController {

    private final RoomService roomService;
    private final MessageService messageService;
    private final IdentityService identityService;

    public ChatWebSocketController(RoomService roomService,
                                    MessageService messageService,
                                    IdentityService identityService) {
        this.roomService = roomService;
        this.messageService = messageService;
        this.identityService = identityService;
    }

    /**
     * Client sends to /app/rooms/{roomId}/send with the token
     * passed as a STOMP header set on the send call
     * Broadcasts the following message to /topic/rooms/{roomId}
     */
    @MessageMapping("/rooms/{roomId}/send")
    public void send(@DestinationVariable UUID roomId,
                      @Header("token") String token,
                      @Valid @Payload SendMessageRequest request) {
        Identity sender = identityService.resolveByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or missing identity token"));
        Room room = roomService.getRoomOrThrow(roomId);
        messageService.sendMessage(room, sender, request.text());
    }
}
