package com.chatapp.controller;

import com.chatapp.dto.CreateRoomRequest;
import com.chatapp.dto.MessageResponse;
import com.chatapp.dto.RoomResponse;
import com.chatapp.entity.Identity;
import com.chatapp.entity.Room;
import com.chatapp.security.ClientIpResolver;
import com.chatapp.security.TokenAuthFilter;
import com.chatapp.service.MessageService;
import com.chatapp.service.PresenceService;
import com.chatapp.service.RoomService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final MessageService messageService;
    private final PresenceService presenceService;

    public RoomController(RoomService roomService, MessageService messageService, PresenceService presenceService) {
        this.roomService = roomService;
        this.messageService = messageService;
        this.presenceService = presenceService;
    }

    @GetMapping
    public List<RoomResponse> list() {
        return roomService.listOpenRooms();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateRoomRequest request, HttpServletRequest httpRequest) {
        Identity identity = (Identity) httpRequest.getAttribute(TokenAuthFilter.IDENTITY_ATTRIBUTE);
        if (identity == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            String ip = ClientIpResolver.resolve(httpRequest);
            RoomResponse room = roomService.createRoom(identity, ip, request);
            return ResponseEntity.ok(room);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(429).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public RoomResponse get(@PathVariable UUID id) {
        Room room = roomService.getRoomOrThrow(id);
        return roomService.toResponse(room);
    }

    @GetMapping("/{id}/messages")
    public List<MessageResponse> messages(@PathVariable UUID id) {
        return messageService.history(id);
    }

    @GetMapping("/{id}/occupants")
    public List<String> occupants(@PathVariable UUID id) {
        return presenceService.occupantsOf(id);
    }
}
