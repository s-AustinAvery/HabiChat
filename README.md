# HabiChat

A full stack rewrite of an old raw socket chat client/server,
redesigned as a modern client server system with a REST + WebSocket API and
a downloadable desktop client.

## Why this project exists

The original version was a bare Python socket client/server.
This rewrite keeps the original spirit but rebuilds it with proper
architecture using a Spring Boot backend exposing a REST API and a live
WebSocket feed, and a React frontend packaged as an Electron desktop app.

## Features

- **Anonymous, persistent identity** - a random animal themed username
  is generated on first launch and stored locally so no accounts or passwords.
- **Rooms** - a permanent default **Lobby**, plus user 
  created rooms (one per identity).
- **Text messaging** - 150 character limit, sanitized server side.
- **Live presence** - see who's currently in a room with you, with
  '{User} has joined'/'{User} has left' server notification, powered
  by hooking into the WebSocket/STOMP session lifecycle.
- **Live room list** - the Main menu screen updates in real time
  as rooms are created, closed, or gain/lose occupants.
- **Downloadable desktop client** - the same React app, packaged
  with Electron.

## Tech stack

- **Backend:** Java, Spring Boot (Web, WebSocket/STOMP, Data JPA)
- **Dev DB:** H2 (will swap to Postgres later for deploy)
- **Frontend:** React, Vite
- **Desktop packaging:** Electron
- **Realtime transport:** STOMP over WebSocket

## REST API (draft)

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/identity` | Create a new identity, returns `{ username, token }` |
| GET | `/api/identity/me` | Resolve current identity from token |
| GET | `/api/rooms` | List open rooms (Lobby + active user rooms) |
| POST | `/api/rooms` | Create a room (enforces 1-per-identity + IP rate limit) |
| GET | `/api/rooms/{id}` | Room details (name, expiresAt) |
| GET | `/api/rooms/{id}/messages` | Message history for a room (paginated) |
| GET | `/api/catalog` | List of premade phrases |

## WebSocket (STOMP) - draft

| Destination | Direction | Purpose |
|---|---|---|
| `/app/rooms/{id}/send` | client -> server | Send a catalog message (by catalogEntryId) into a room |
| `/topic/rooms/{id}` | server -> client | Broadcast new messages to room subscribers |
| `/topic/rooms/{id}/system` | server -> client | Join/leave/room-closing notifications |

## Running locally

### Backend
```
cd backend
./mvnw spring-boot:run
```
Runs on `http://localhost:8080`, using an in memory H2 database (no setup
required). If `./mvnw` isn't present, run `mvn -N wrapper:wrapper` once to
generate it, or run `HabiChatApplication` directly from your IDE.

### Frontend (web, for development)
```
cd frontend
npm install
npm run dev
```

### Desktop (Electron)
```
cd electron
npm install
npm start
```

## Status

Core features are working such as identity, rooms, presence,
live messaging, and desktop packaging via Electron.

## Roadmap

- [ ] Visuals update (currently unstyled)
- [ ] Avatars for connected users
- [ ] Swap H2 -> Postgres and deploy the backend
- [ ] Backend tests
- [ ] Maven wrapper committed
