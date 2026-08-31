import React, { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import { getClient } from '../ws/socket.js';
import { getToken, getUsername } from '../api/identity.js';
import MessageFeed from '../components/MessageFeed.jsx';
import MessageInput from '../components/MessageInput.jsx';
import UserList from '../components/UserList.jsx';

const MAX_VISIBLE_MESSAGES = 150;

function trimToRetention(events, limit) {
  let messageCount = 0;
  for (let i = events.length - 1; i >= 0; i--) {
    if (events[i].kind === 'message') {
      messageCount++;
      if (messageCount > limit) {
        return events.slice(i + 1);
      }
    }
  }
  return events;
}

export default function RoomPage({ roomId, onRoomClosed }) {
  const [events, setEvents] = useState([]);
  const [occupants, setOccupants] = useState([]);
  const [client, setClient] = useState(null);

  useEffect(() => {
    let cancelled = false;
    let messageSub = null;
    let systemSub = null;

    api.roomMessages(roomId).then((history) => {
      if (cancelled) return;
      setEvents(trimToRetention(history.map((m) => ({ kind: 'message', ...m })), MAX_VISIBLE_MESSAGES));
    });

    getClient().then((c) => {
      if (cancelled) return;
      setClient(c);

      // Subscribe to the system topic
      systemSub = c.subscribe(`/topic/rooms/${roomId}/system`, (frame) => {
        const notice = JSON.parse(frame.body);

        if (notice.type === 'ROOM_CLOSED') {
          onRoomClosed?.();
          return;
        }
        if (notice.type === 'ROOM_OCCUPANTS') {
          setOccupants(notice.users);
          return;
        }
        // USER_JOINED / USER_LEFT for logging
        // Room broadcast is done by ROOM_OCCUPANTS
        setEvents((prev) =>
          trimToRetention(
            [...prev, { kind: 'system', id: crypto.randomUUID(), ...notice }],
            MAX_VISIBLE_MESSAGES
          )
        );
      });

      messageSub = c.subscribe(
        `/topic/rooms/${roomId}`,
        (frame) => {
          const message = JSON.parse(frame.body);
          setEvents((prev) => 
            trimToRetention([...prev, { kind: 'message', ...message }], MAX_VISIBLE_MESSAGES)
          );
        },
        { token: getToken() ?? '' }
      );
    });

    return () => {
      cancelled = true;
      messageSub?.unsubscribe();
      systemSub?.unsubscribe();
    };
  }, [roomId]);

  function sendMessage(text) {
    client?.publish({
      destination: `/app/rooms/${roomId}/send`,
      body: JSON.stringify({ text }),
      headers: { token: getToken() ?? '' },
    });
  }

  return (
    <div className="room-page-body">
      <div className="room-page">
        <MessageFeed events={events} currentUsername={getUsername()} />
        <MessageInput onSend={sendMessage} />
      </div>
      <UserList users={occupants} />
    </div>
  );
}
