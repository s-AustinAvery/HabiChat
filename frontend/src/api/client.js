const BASE_URL = 'http://localhost:8080/api';

function authHeaders() {
  const token = localStorage.getItem('chatapp_token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
      ...(options.headers || {}),
    },
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(text || `Request failed: ${res.status}`);
  }
  return res.status === 204 ? null : res.json();
}

export const api = {
  createIdentity: () => request('/identity', { method: 'POST' }),
  me: () => request('/identity/me'),
  listRooms: () => request('/rooms'),
  createRoom: (name) => request('/rooms', { method: 'POST', body: JSON.stringify({ name }) }),
  getRoom: (id) => request(`/rooms/${id}`),
  roomMessages: (id) => request(`/rooms/${id}/messages`),
  roomOccupants: (id) => request(`/rooms/${id}/occupants`),
  presence: () => request('/presence'),
};
