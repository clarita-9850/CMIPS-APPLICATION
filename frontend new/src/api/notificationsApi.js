import http from './httpClient';

export const getUserNotifications = (userId) =>
  http.get(`/notifications/user/${userId}`).then(r => r.data);

export const getUnreadCount = (userId) =>
  http.get(`/notifications/unread-count/${userId}`).then(r => r.data);

export const markAsRead = (id) =>
  http.put(`/notifications/${id}/read`).then(r => r.data);

export const markAllRead = (userId) =>
  http.put(`/notifications/user/${userId}/read-all`).then(r => r.data);

export const createNotification = (data) =>
  http.post('/notifications', data).then(r => r.data);

export const deleteNotification = (id) =>
  http.delete(`/notifications/${id}`).then(r => r.data);
