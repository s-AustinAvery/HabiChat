# HabiChat

A full stack rewrite of an old raw socket chat client/server,
redesigned as a modern client server system with a REST + WebSocket API and
a downloadable desktop client.

## Features

- **Anonymous/persistent identity** - a random animal themed username
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
- **Frontend:** React (Vite)
- **Desktop packaging:** Electron
- **Realtime transport:** WebSocket over STOMP

## REST API (current model)

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/identity` | Create a new identity and returns `{ username, token }` |
| GET | `/api/identity/me` | Resolve current identity from token |
| GET | `/api/rooms` | List open rooms |
| POST | `/api/rooms` | Create a room |
| GET | `/api/rooms/{id}` | Room details |
| GET | `/api/rooms/{id}/messages` | Message history for a room |
| GET | `/api/rooms/{id}/occupants` | Snapshot of users currently in a room |
| GET | `/api/rooms/{id}/presence` | Snapshot of users connected to the server (currently unused) |

## WebSocket (STOMP) (current model)

| Destination | Direction | Purpose |
|---|---|---|
| `/app/rooms/{id}/send` | client -> server | Send a text message ({ text }) into a room |
| `/topic/rooms/{id}` | server -> client | USER_JOINED / USER_LEFT / ROOM_OCCUPANTS / ROOM_CLOSED |
| `/topic/rooms/{id}/system` | server -> client | Join/leave/room closing notifications |
| `/topic/rooms` | server -> client | Full open room list (broadcast on any create/close/occupancy change) |
| `/topic/presence` | server -> client | Global connected users list (currently unused) |

## Running locally

### Backend
```
cd backend
./mvnw spring-boot:run
```
Runs on `http://localhost:8080`, using an in memory H2 database. 
If `./mvnw` isn't present, run `mvn -N wrapper:wrapper` once to
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
