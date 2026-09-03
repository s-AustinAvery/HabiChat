package com.habichat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In memory rate limiter for room creation
 */
@Component
public class RoomRateLimiter {

    private final int maxPerHour;
    private final Map<String, Deque<Instant>> attemptsByIp = new ConcurrentHashMap<>();

    // 3 rooms can be made per ip but each user/token can only make 1 room
    public RoomRateLimiter(@Value("${habichat.room.creation-rate-limit-per-ip-per-hour:3}") int maxPerHour) {
        this.maxPerHour = maxPerHour;
    }

    public synchronized boolean tryConsume(String ip) {
        Instant now = Instant.now();
        Deque<Instant> attempts = attemptsByIp.computeIfAbsent(ip, k -> new ArrayDeque<>());

        // drop anything older than an hour
        while (!attempts.isEmpty() && Duration.between(attempts.peekFirst(), now).toHours() >= 1) {
            attempts.pollFirst();
        }

        if (attempts.size() >= maxPerHour) {
            return false;
        }

        attempts.addLast(now);
        return true;
    }
}
