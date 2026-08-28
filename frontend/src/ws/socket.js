import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getToken } from '../api/identity.js';

let client = null;
let connectPromise = null;

/**
 * Returns a shared connected STOMP client, creating and connecting it on first call
 * Every component that needs the socket shares this connection instead of each opening its own
 */
export function getClient() {
  if (client) return Promise.resolve(client);
  if (connectPromise) return connectPromise;

  connectPromise = new Promise((resolve) => {
    client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: { token: getToken() ?? '' },
      reconnectDelay: 3000,
      onConnect: () => resolve(client),
    });
    client.activate();
  });

  return connectPromise;
}
