import React, { useState } from 'react';

export default function RoomList({ rooms, onSelect, onCreate }) {
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');

  function submit(e) {
    e.preventDefault();
    if (!name.trim()) return;
    onCreate(name.trim());
    setName('');
    setShowForm(false);
  }

  return (
    <div className="room-list">
      <h2>Rooms</h2>
      <ul>
        {rooms.map((room) => (
          <li key={room.id}>
            <button onClick={() => onSelect(room.id)}>
              {room.name}<span className="room-count">{room.occupantCount}</span>
            </button>
          </li>
        ))}
      </ul>

      {showForm ? (
        <form onSubmit={submit}>
          <input
            autoFocus
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Room name"
            maxLength={40}
          />
          <button type="submit">Create</button>
          <button type="button" onClick={() => setShowForm(false)}>Cancel</button>
        </form>
      ) : (
        <button onClick={() => setShowForm(true)}>+ Create room</button>
      )}
    </div>
  );
}
