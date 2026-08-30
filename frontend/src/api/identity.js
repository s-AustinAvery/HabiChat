import { api } from './client.js';

const TOKEN_KEY = 'chatapp_token';
const USERNAME_KEY = 'chatapp_username';

export async function ensureIdentity() {
  const existingToken = localStorage.getItem(TOKEN_KEY);

  if (existingToken) {
    // Check the token is still recognized by the sever
    // A server restart since this token was used makes
    // the old token no longer work, so generate a new one
    try {
      return await api.me();
    } catch {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USERNAME_KEY);
    }
  }

  const identity = await api.createIdentity();
  localStorage.setItem(TOKEN_KEY, identity.token);
  localStorage.setItem(USERNAME_KEY, identity.username);
  return identity;
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function getUsername() {
  return localStorage.getItem(USERNAME_KEY);
}
