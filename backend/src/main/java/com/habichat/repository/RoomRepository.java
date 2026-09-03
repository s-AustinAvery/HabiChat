package com.habichat.repository;

import com.habichat.entity.Identity;
import com.habichat.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    Optional<Room> findByIsDefaultTrue();

    Optional<Room> findByCreatedBy(Identity createdBy);

    // rooms that are still open = default room (Lobby) OR have not yet expired
    List<Room> findByIsDefaultTrueOrExpiresAtAfter(Instant now);

    List<Room> findByIsDefaultFalseAndExpiresAtBefore(Instant now);
}
