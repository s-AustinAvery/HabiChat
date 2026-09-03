package com.habichat.websocket;

import com.habichat.entity.Identity;
import com.habichat.service.IdentityService;
import com.habichat.service.PresenceService;
import com.habichat.service.RoomService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listens to the STOMP session lifecycle to drive presence:
 *  - SessionSubscribeEvent/SessionUnsubscribeEvent makes a
 *    '[User] has joined' / '[User] has left' notice to the room and
 *    a fresh participants snapshot broadcast on /topic/rooms/{id}/system
 *  - Any room join/leave also rebroadcasts the full room list with
 *    updated participants count on /topic/rooms for anyone in the lobby
 */
@Component
public class WebSocketEventListener {

    // matches /topic/rooms/{uuid} so presence tracking ignores the system notice channel
    private static final Pattern ROOM_TOPIC =
            Pattern.compile("^/topic/rooms/([0-9a-fA-F-]{36})$");

    private final IdentityService identityService;
    private final PresenceService presenceService;
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(IdentityService identityService,
                                   PresenceService presenceService,
                                   RoomService roomService,
                                   SimpMessagingTemplate messagingTemplate) {
        this.identityService = identityService;
        this.presenceService = presenceService;
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String token = accessor.getFirstNativeHeader("token");
        identityService.resolveByToken(token).ifPresent(identity -> {
            presenceService.connect(accessor.getSessionId(), identity);
            broadcastGlobalPresence();
        });
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        var leftRooms = presenceService.leaveAllRoomsForSession(sessionId);
        leftRooms.forEach(sub -> {
            announce(sub.roomId(), sub.identity().getUsername(), "USER_LEFT");
            broadcastOccupants(sub.roomId());
        });
        if (!leftRooms.isEmpty()) {
            roomService.broadcastRoomList();
        }

        Identity identity = presenceService.disconnect(sessionId);
        if (identity != null) {
            broadcastGlobalPresence();
        }
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Matcher matcher = ROOM_TOPIC.matcher(String.valueOf(accessor.getDestination()));
        if (!matcher.matches()) {
            return;
        }

        String token = accessor.getFirstNativeHeader("token");
        identityService.resolveByToken(token).ifPresent(identity -> {
            UUID roomId = UUID.fromString(matcher.group(1));
            presenceService.joinRoom(accessor.getSessionId(), accessor.getSubscriptionId(), roomId, identity);
            announce(roomId, identity.getUsername(), "USER_JOINED");
            broadcastOccupants(roomId);
            roomService.broadcastRoomList();
        });
    }

    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        presenceService.leaveRoom(accessor.getSessionId(), accessor.getSubscriptionId())
                .ifPresent(sub -> {
                    announce(sub.roomId(), sub.identity().getUsername(), "USER_LEFT");
                    broadcastOccupants(sub.roomId());
                    roomService.broadcastRoomList();
                });
    }

    private void announce(UUID roomId, String username, String type) {
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/system",
                Map.of("type", type, "username", username)
        );
    }

    /**
     * Sends a fresh occupant list to a room's system topic
     */
    private void broadcastOccupants(UUID roomId) {
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/system",
                Map.of("type", "ROOM_OCCUPANTS", "users", presenceService.occupantsOf(roomId))
        );
    }

    private void broadcastGlobalPresence() {
        messagingTemplate.convertAndSend("/topic/presence", presenceService.connectedUsernames());
    }
}
