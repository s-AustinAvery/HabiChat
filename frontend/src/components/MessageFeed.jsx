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
            <span className="text">{e.text}</span>
          </div>
        )
      )}
      <div ref={bottomRef} />
    </div>
  );
}
