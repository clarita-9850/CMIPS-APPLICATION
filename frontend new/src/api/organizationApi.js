import http from './httpClient';

const base = '/admin/keycloak';

// Users
export const getUsers = () => http.get(`${base}/users`).then(r => r.data);
export const createUser = (data) => http.post(`${base}/users`, data).then(r => r.data);
export const deleteUser = (userId) => http.delete(`${base}/users/${userId}`).then(r => r.data);
export const getUserRoles = (userId) => http.get(`${base}/users/${userId}/roles`).then(r => r.data);
export const assignRole = (userId, roleName) => http.post(`${base}/users/${userId}/roles`, { roleName }).then(r => r.data);
export const getUserGroups = (userId) => http.get(`${base}/users/${userId}/groups`).then(r => r.data);
export const addUserToGroup = (userId, groupId) => http.put(`${base}/users/${userId}/groups/${groupId}`).then(r => r.data);
export const removeUserFromGroup = (userId, groupId) => http.delete(`${base}/users/${userId}/groups/${groupId}`).then(r => r.data);

// Roles
export const getRoles = () => http.get(`${base}/roles`).then(r => r.data);
export const createRole = (roleName, description) => http.post(`${base}/roles`, { roleName, description }).then(r => r.data);
export const deleteRole = (roleId) => http.delete(`${base}/roles/${roleId}`).then(r => r.data);

// Groups
export const getGroups = () => http.get(`${base}/groups`).then(r => r.data);
export const createGroup = (groupName) => http.post(`${base}/groups`, { groupName }).then(r => r.data);
export const deleteGroup = (groupId) => http.delete(`${base}/groups/${groupId}`).then(r => r.data);
export const getGroupMembers = (groupId) => http.get(`${base}/groups/${groupId}/members`).then(r => r.data);

// Resources & Permissions
export const getResources = () => http.get(`${base}/resources`).then(r => r.data);
export const getPolicies = () => http.get(`${base}/policies`).then(r => r.data);
export const getPermissions = () => http.get(`${base}/permissions`).then(r => r.data);
