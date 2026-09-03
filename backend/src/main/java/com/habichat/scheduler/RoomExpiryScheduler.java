package com.habichat.scheduler;

import com.habichat.entity.Room;
import com.habichat.service.RoomService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Occasionally closes user created rooms that have passed their expiry (24 hours)
 */
@Component
public class RoomExpiryScheduler {

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomExpiryScheduler(RoomService roomService, SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(fixedRateString = "PT5M") // called every 5 minutes
    public void sweepExpiredRooms() {
        List<Room> expired = roomService.findExpiredRooms();
        for (Room room : expired) {
            // let anyone still connected know before it disappears
            messagingTemplate.convertAndSend(
                    "/topic/rooms/" + room.getId() + "/system",
                    Map.of("type", "ROOM_CLOSED", "roomId", room.getId().toString())
            );
            roomService.closeRoom(room);
        }
    }
}
