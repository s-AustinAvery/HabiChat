CREATE TABLE identities (
    id         UUID PRIMARY KEY,
    username   VARCHAR(255) NOT NULL,
    token      VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    CONSTRAINT uq_identities_token UNIQUE (token)
);

CREATE TABLE rooms (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    is_default  BOOLEAN      NOT NULL,
    created_by  UUID         NULL REFERENCES identities (id),
    created_at  TIMESTAMP    NOT NULL,
    expires_at  TIMESTAMP    NULL
);

CREATE TABLE messages (
    id         UUID PRIMARY KEY,
    room_id    UUID         NOT NULL REFERENCES rooms (id),
    sender_id  UUID         NOT NULL REFERENCES identities (id),
    text       VARCHAR(150) NOT NULL,
    sent_at    TIMESTAMP    NOT NULL
);

-- Supports RoomRepository.findByCreatedBy and the 1-room-per-identity check
CREATE INDEX idx_rooms_created_by ON rooms (created_by);

-- Supports RoomRepository's open-room queries
CREATE INDEX idx_rooms_is_default ON rooms (is_default);
CREATE INDEX idx_rooms_expires_at ON rooms (expires_at);

-- Supports MessageRepository.findByRoomIdOrderBySentAtAsc (history load + retention pruning)
CREATE INDEX idx_messages_room_id_sent_at ON messages (room_id, sent_at);
