import React from 'react';

export default function UserList({ users }) {
  return (
    <aside className="user-list">
      <h2>Online ({users.length})</h2>
      <ul>
        {users.map((username) => (
          <li key={username}>
            <span className="presence-dot" />
            {username}
          </li>
        ))}
      </ul>
    </aside>
  );
}
