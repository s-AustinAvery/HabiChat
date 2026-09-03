package com.habichat.service;

import com.habichat.dto.IdentityResponse;
import com.habichat.entity.Identity;
import com.habichat.repository.IdentityRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IdentityService {

    private final IdentityRepository identityRepository;
    private final SecureRandom random = new SecureRandom();

    // Random username first half
    private static final List<String> ADJECTIVES = List.of(
            "Swift", "Brave", "Lucky", "Moody", "Bright", "Witty",
            "Gentle", "Bold", "Lively", "Nimble", "Calm", "Small", "Big", "Lax", "Quirky",
            "Timid", "Hardy", "Cool", "Loud", "Quiet"
    );
    // Random username second half
    private static final List<String> ANIMALS = List.of(
            "Cat", "Dog", "Falcon", "Bear", "Koala", "Fox", "Tiger", "Wolf",
            "Otter", "Badger", "Saber", "Dragon", "Raccoon", "Rabbit", "Penguin"
    );

    public IdentityService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public IdentityResponse createIdentity() {
        String username = generateUsername();
        String token = UUID.randomUUID().toString();

        Identity identity = Identity.builder()
                .username(username)
                .token(token)
                .build();

        identityRepository.save(identity);
        return new IdentityResponse(identity.getUsername(), identity.getToken());
    }

    public Optional<Identity> resolveByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return identityRepository.findByToken(token);
    }

    private String generateUsername() {
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));
        int suffix = random.nextInt(90) + 10; // 2 digits at the end to help with variation
        return adjective + animal + suffix;
    }
}
