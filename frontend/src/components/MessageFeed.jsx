import React, { useEffect, useRef } from 'react';

function systemText(notice) {
  switch (notice.type) {
    case 'USER_JOINED':
      return `${notice.username} has joined`;
    case 'USER_LEFT':
      return `${notice.username} has left`;
    default:
      return null;
  }
}

function formatTime(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
}

export default function MessageFeed({ events, currentUsername }) {
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ block: 'nearest' });
  }, [events]);

  return (
    <div className="message-feed">
      {events.map((e) =>
        e.kind === 'system' ? (
          <div key={e.id} className="system-notice">
            {systemText(e)}
          </div>
        ) : (
          <div
            key={e.id}
            className={`message ${e.senderUsername === currentUsername ? 'message--own' : ''}`}
          >
            <span className="sender">{e.senderUsername}</span>
            <span>{e.text}</span>
            <span className="time">{formatTime(e.sentAt)}</span>
          </div>
        )
      )}
      <div ref={bottomRef} />
    </div>
  );
}
