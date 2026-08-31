import React, { useEffect, useState } from 'react';
import { api } from './api/client.js';
import { ensureIdentity } from './api/identity.js';
import { getClient } from './ws/socket.js';
import RoomList from './components/RoomList.jsx';
import RoomPage from './pages/RoomPage.jsx';

const LOBBY_REFRESH_INTERVAL_MS = 15000;

export default function App() {
  const [ready, setReady] = useState(false);
  const [username, setUsername] = useState(null);
  const [rooms, setRooms] = useState([]);
  const [activeRoomId, setActiveRoomId] = useState(null);

  useEffect(() => {
    ensureIdentity().then((identity) => {
      setUsername(identity.username);
      setReady(true);
    });
  }, []);

  function refreshRooms() {
    api.listRooms().then(setRooms);
  }

  useEffect(() => {
    if (!ready || activeRoomId) return;

    let cancelled = false;
    let sub = null;

    refreshRooms();
    const interval = setInterval(refreshRooms, LOBBY_REFRESH_INTERVAL_MS);

    getClient().then((client) => {
      if (cancelled) return;
      sub = client.subscribe('/topic/rooms', (frame) => {
        setRooms(JSON.parse(frame.body));
      });
    });

    return () => {
      cancelled = true;
      clearInterval(interval);
      sub?.unsubscribe();
    };
  }, [ready, activeRoomId]);

  async function handleCreateRoom(name) {
    try {
      await api.createRoom(name);
      // Creating a room triggers a topic/rooms broadcast
    } catch (e) {
      alert(e.message);
    }
  }

  function handleRoomClosed() {
    setActiveRoomId(null);
  }

  if (!ready) return <div>Loading...</div>;

  const activeRoom = rooms.find((r) => r.id === activeRoomId);

  return (
    <div className="app">
      <header>
        <h1>HabiChat</h1>
        <span>Signed in as {username}</span>
      </header>
      {activeRoomId ? (
        <>
          <button className="back-button" onClick={() => setActiveRoomId(null)}>&#8617; Leave {activeRoom?.name ?? 'room'}</button>
          <RoomPage roomId={activeRoomId} onRoomClosed={handleRoomClosed} />
        </>
      ) : (
        <RoomList rooms={rooms} onSelect={setActiveRoomId} onCreate={handleCreateRoom} />
      )}
    </div>
  );
}
