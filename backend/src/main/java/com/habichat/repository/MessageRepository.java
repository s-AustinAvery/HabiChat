package com.habichat.repository;

import com.habichat.entity.Message;
import com.habichat.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByRoomIdOrderBySentAtAsc(UUID roomId);
    void deleteByRoom(Room room);
}
