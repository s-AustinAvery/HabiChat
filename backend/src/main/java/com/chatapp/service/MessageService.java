package com.chatapp.service;

import com.chatapp.constants.MessageConstraints;
import com.chatapp.dto.MessageResponse;
import com.chatapp.entity.Identity;
import com.chatapp.entity.Message;
import com.chatapp.entity.Room;
import com.chatapp.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class MessageService {

    // Strips control characters so a message cant
    // inject terminal/log control commands
    private static final Pattern CONTROL_CHARS = Pattern.compile("\\p{Cntrl}");

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final int retentionLimit;

    public MessageService(MessageRepository messageRepository,
                          SimpMessagingTemplate messagingTemplate,
                          @Value("${chatapp.message.retention-limit:150}") int retentionLimit) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
        this.retentionLimit = retentionLimit;
    }

    public List<MessageResponse> history(UUID roomId) {
        return messageRepository.findByRoomIdOrderBySentAtAsc(roomId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Called from the WebSocket controller when a client sends a message
     * Revalidates and sanitizes server side as an enforcement point.
     */
    public void sendMessage(Room room, Identity sender, String rawText) {
        String text = sanitize(rawText);

        if (text.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }
        if (text.length() > MessageConstraints.MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Message exceeds max length of " + MessageConstraints.MAX_LENGTH + " characters.");
        }

        Message message = Message.builder()
                .room(room)
                .sender(sender)
                .text(text)
                .build();
        messageRepository.save(message);
        pruneOldMessages(room);

        MessageResponse response = toResponse(message);
        messagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), response);
    }

    /**
     * room history limited to specific number of messages
     * deleting anything past the max message count
     */
    private void pruneOldMessages(Room room) {
        List<Message> ordered = messageRepository.findByRoomIdOrderBySentAtAsc(room.getId());
        int overflow = ordered.size() - retentionLimit;
        if (overflow > 0) {
            messageRepository.deleteAll(ordered.subList(0, overflow));
        }
    }

    private String sanitize(String raw) {
        if (raw == null) {
            return "";
        }
        return CONTROL_CHARS.matcher(raw).replaceAll("").trim();
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getRoom().getId(),
                message.getSender().getUsername(),
                message.getText(),
                message.getSentAt()
        );
    }
}
