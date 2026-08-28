import React, { useState } from 'react';

const MAX_LENGTH = 150;

export default function MessageInput({ onSend }) {
  const [text, setText] = useState('');

  function submit(e) {
    e.preventDefault();
    const trimmed = text.trim();
    if (!trimmed) return;
    onSend(trimmed);
    setText('');
  }

  const remaining = MAX_LENGTH - text.length;

  return (
    <form onSubmit={submit} className="message-input">
      <input
        value={text}
        onChange={(e) => setText(e.target.value.slice(0, MAX_LENGTH))}
        placeholder="Write a message..."
        maxLength={MAX_LENGTH}
      />
      <span className="char-count">{remaining}</span>
      <button type="submit" disabled={!text.trim()}>Send</button>
    </form>
  );
}
