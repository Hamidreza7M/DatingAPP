/** Shared API adapter for Android, Responsive Web and PWA.
 * Set window.FASTLOVE_API_BASE before main.js to point to the central API.
 * The current UI demo intentionally uses repository.js until all resources are implemented server-side.
 */
export class FastLoveApiClient {
  constructor(baseUrl = globalThis.FASTLOVE_API_BASE || '') {
    this.baseUrl = String(baseUrl || '').replace(/\/$/, '');
  }
  get remoteEnabled() { return Boolean(this.baseUrl); }
  async request(path, options = {}) {
    if (!this.remoteEnabled) throw new Error('Central API is not configured; demo repository is active.');
    const isForm = typeof FormData !== 'undefined' && options.body instanceof FormData;
    const headers = { ...(isForm ? {} : { 'Content-Type': 'application/json' }), ...(options.headers || {}) };
    const response = await fetch(`${this.baseUrl}${path}`, {
      ...options,
      headers,
      credentials: 'include',
    });
    if (!response.ok) {
      const message = await response.text().catch(() => '');
      throw new Error(message || `API request failed (${response.status})`);
    }
    if (response.status === 204) return null;
    return response.json();
  }
  health() { return this.request('/v1/health'); }
  sendOtp(phone) { return this.request('/v1/auth/otp', { method: 'POST', body: JSON.stringify({ phone }) }); }
  verifyOtp(phone, code) { return this.request('/v1/auth/verify', { method: 'POST', body: JSON.stringify({ phone, code }) }); }
  getProfile() { return this.request('/v1/me'); }
  updateProfile(profile) { return this.request('/v1/me', { method: 'PATCH', body: JSON.stringify(profile) }); }
  getDiscovery(filters = {}) { return this.request(`/v1/discovery?${new URLSearchParams(filters)}`); }
  getLikes() { return this.request('/v1/likes'); }
  recordLike(userId, action = 'like') { return this.request('/v1/likes', { method: 'POST', body: JSON.stringify({ userId, action }) }); }
  getChats() { return this.request('/v1/chats'); }
  getMessages(chatId) { return this.request(`/v1/chats/${encodeURIComponent(chatId)}/messages`); }
  sendMessage(chatId, formData) { return this.request(`/v1/chats/${encodeURIComponent(chatId)}/messages`, { method: 'POST', body: formData }); }
  getDates() { return this.request('/v1/dates'); }
  createDate(proposal) { return this.request('/v1/dates', { method: 'POST', body: JSON.stringify(proposal) }); }
  updateDate(dateId, status) { return this.request(`/v1/dates/${encodeURIComponent(dateId)}`, { method: 'PATCH', body: JSON.stringify({ status }) }); }
  getPackages() { return this.request('/v1/packages'); }
  getPurchases() { return this.request('/v1/purchases'); }
  startPurchase(packageId, idempotencyKey) { return this.request('/v1/purchases', { method: 'POST', body: JSON.stringify({ packageId, idempotencyKey }) }); }
  getPrivacy() { return this.request('/v1/privacy'); }
  updatePrivacy(settings) { return this.request('/v1/privacy', { method: 'PATCH', body: JSON.stringify(settings) }); }
  getSupportTickets() { return this.request('/v1/support/tickets'); }
  createSupportTicket(formData) { return this.request('/v1/support/tickets', { method: 'POST', body: formData }); }
  requestAccountDeletion() { return this.request('/v1/me', { method: 'DELETE' }); }
}
