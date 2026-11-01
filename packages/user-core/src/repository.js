import { createInitialState } from './data.js';

const STORAGE_KEY = 'fastlove.shared.user-state.v1';

function safeParse(value) {
  try { return value ? JSON.parse(value) : null; } catch { return null; }
}

export function loadState() {
  const saved = safeParse(localStorage.getItem(STORAGE_KEY));
  if (!saved || typeof saved !== 'object') return createInitialState();
  const defaults = createInitialState();
  return { ...defaults, ...saved, filters: { ...defaults.filters, ...(saved.filters || {}) }, subscription: { ...defaults.subscription, ...(saved.subscription || {}) } };
}

export function saveState(state) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

export function resetState() {
  const fresh = createInitialState();
  saveState(fresh);
  return fresh;
}

export const Repository = Object.freeze({ load: loadState, save: saveState, reset: resetState });
