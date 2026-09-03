package com.habichat.repository;

import com.habichat.entity.Identity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdentityRepository extends JpaRepository<Identity, UUID> {
    Optional<Identity> findByToken(String token);
}
