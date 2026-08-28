package com.chatapp.service;

import com.chatapp.entity.Identity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In memory presence tracking of who is currently connected to the server
 * and who is currently subscribed/actively in which room.
 */
@Component
public class PresenceService {

    public record RoomSubscription(String sessionId, UUID roomId, Identity identity) {
    }

    private final Map<String, Identity> connectedBySession = new ConcurrentHashMap<>();
    private final Map<String, RoomSubscription> roomSubscriptions = new ConcurrentHashMap<>();

    /**
     * subscriptionId is generated independently by each client STOMP library.
     * sessionId is assigned by the server and is globally unique.
     * they are combined to guarantee a collision free key across all connected users.
     */
    private static String key(String sessionId, String subscriptionId) {
        return sessionId + "|" + subscriptionId;
    }

    public void connect(String sessionId, Identity identity) {
        connectedBySession.put(sessionId, identity);
    }

    public Identity disconnect(String sessionId) {
        return connectedBySession.remove(sessionId);
    }

    public List<String> connectedUsernames() {
        return connectedBySession.values().stream()
                .map(Identity::getUsername)
                .distinct()
                .sorted()
                .toList();
    }

    public void joinRoom(String sessionId, String subscriptionId, UUID roomId, Identity identity) {
        roomSubscriptions.put(key(sessionId, subscriptionId), new RoomSubscription(sessionId, roomId, identity));
    }

    public Optional<RoomSubscription> leaveRoom(String sessionId, String subscriptionId) {
        return Optional.ofNullable(roomSubscriptions.remove(key(sessionId, subscriptionId)));
    }

    public List<String> occupantsOf(UUID roomId) {
        return roomSubscriptions.values().stream()
                .filter(sub -> sub.roomId().equals(roomId))
                .map(sub -> sub.identity().getUsername())
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Called on disconnect to clean up any room subscriptions
     * the session never explicitly unsubscribed from.
     * */
    public List<RoomSubscription> leaveAllRoomsForSession(String sessionId) {
        List<RoomSubscription> left = new ArrayList<>();
        roomSubscriptions.entrySet().removeIf(entry -> {
            boolean matches = entry.getValue().sessionId().equals(sessionId);
            if (matches) {
                left.add(entry.getValue());
            }
            return matches;
        });
        return left;
    }
}
