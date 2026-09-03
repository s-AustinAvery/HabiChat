package com.habichat.service;

import com.habichat.dto.CreateRoomRequest;
import com.habichat.dto.RoomResponse;
import com.habichat.entity.Identity;
import com.habichat.entity.Room;
import com.habichat.repository.MessageRepository;
import com.habichat.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final RoomRateLimiter rateLimiter;
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final int expiryHours;

    public RoomService(RoomRepository roomRepository,
                        MessageRepository messageRepository,
                        RoomRateLimiter rateLimiter,
                        PresenceService presenceService,
                        SimpMessagingTemplate messagingTemplate,
                        @Value("${habichat.room.expiry-hours:24}") int expiryHours) {
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;
        this.rateLimiter = rateLimiter;
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
        this.expiryHours = expiryHours;
    }

    /**
     * Default Lobby room should always exist on startup
     */
    @PostConstruct
    void ensureLobbyExists() {
        if (roomRepository.findByIsDefaultTrue().isEmpty()) {
            Room lobby = Room.builder()
                    .name("Lobby")
                    .isDefault(true)
                    .createdBy(null)
                    .expiresAt(null)
                    .build();
            roomRepository.save(lobby);
        }
    }

    public List<RoomResponse> listOpenRooms() {
        return roomRepository.findByIsDefaultTrueOrExpiresAtAfter(Instant.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RoomResponse createRoom(Identity creator, String clientIp, CreateRoomRequest request) {
        if (roomRepository.findByCreatedBy(creator).isPresent()) {
            throw new IllegalStateException("You already have an active room.");
        }
        if (!rateLimiter.tryConsume(clientIp)) {
            throw new IllegalStateException("Too many rooms created from this network. Try again later.");
        }

        Room room = Room.builder()
                .name(request.name())
                .isDefault(false)
                .createdBy(creator)
                .expiresAt(Instant.now().plus(expiryHours, ChronoUnit.HOURS))
                .build();

        roomRepository.save(room);
        broadcastRoomList();
        return toResponse(room);
    }

    public Room getRoomOrThrow(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + id));
    }

    /**
     * Called by the scheduled expiry sweep
     */
    public void closeRoom(Room room) {
        messageRepository.deleteByRoom(room);
        roomRepository.delete(room);
        broadcastRoomList();
    }

    public List<Room> findExpiredRooms() {
        return roomRepository.findByIsDefaultFalseAndExpiresAtBefore(Instant.now());
    }

    public void broadcastRoomList() {
        messagingTemplate.convertAndSend("/topic/rooms", listOpenRooms());
    }

    public RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.isDefault(),
                room.getCreatedAt(),
                room.getExpiresAt(),
                presenceService.occupantsOf(room.getId()).size()
        );
    }
}
