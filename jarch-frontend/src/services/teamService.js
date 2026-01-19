import { request } from './api';

export const teamService = {
    async addMember(projectId, memberData) {
        return request(`/team?projectId=${projectId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(memberData)
        });
    },

    async getTeamMembers(projectId) {
        return request(`/team?projectId=${projectId}`);
    },

    async removeMember(projectId, username) {
        return request(`/team?teamMember=${encodeURIComponent(username)}&projectId=${projectId}`, {
            method: 'DELETE'
        });
    }
};