import { request, getToken } from './api';

const API_BASE = 'http://localhost:8080';

export const saveService = {
    async getProjectSaves(projectId) {
        try {
            const saves = await request(`/project-saves/get-all/${projectId}`);
            return Array.isArray(saves) ? saves : [];
        } catch (error) {
            console.error('Ошибка загрузки сохранений:', error);
            return [];
        }
    },

    async getSaving(savingId) {
        return request(`/project-saves/get/${savingId}`);
    },

    async getSavingConfig(savingId) {
        try {
            const config = await request(`/project-saves/config/${savingId}`);
            return config;
        } catch (error) {
            console.error('Ошибка загрузки конфигурации:', error);
            throw error;
        }
    },

    async createSave(projectId, saveName, entityConfigFile, appConfigFile) {
        const formData = new FormData();
        formData.append('saveName', saveName);
        formData.append('projectId', projectId);
        formData.append('entityConfig', entityConfigFile);
        formData.append('appConfig', appConfigFile);

        return request('/project-saves/save', {
            method: 'POST',
            body: formData
        });
    },

    async updateSave(savingId, saveName, entityConfigFile, appConfigFile) {
        const formData = new FormData();
        formData.append('saveName', saveName);
        formData.append('entityConfig', entityConfigFile);
        formData.append('appConfig', appConfigFile);

        return request(`/project-saves/update/${savingId}`, {
            method: 'PUT',
            body: formData
        });
    },

    async deleteSave(savingId) {
        return request(`/project-saves/delete/${savingId}`, {
            method: 'DELETE'
        });
    },

    async downloadEntityConfig(savingId) {
        const response = await fetch(`${API_BASE}/project-saves/download-entity/${savingId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${getToken()}`
            }
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Ошибка загрузки файла');
        }
        
        return await response.blob();
    },

    async downloadAppConfig(savingId) {
        const response = await fetch(`${API_BASE}/project-saves/download-app/${savingId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${getToken()}`
            }
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Ошибка загрузки файла');
        }
        
        return await response.blob();
    },

    async downloadProjectEntity(saveName, projectName) {
        try {
            console.warn('Этот метод устарел. Используйте downloadEntityConfig(savingId)');
            return request(`/jarch/download-entity-file/${encodeURIComponent(projectName)}/${encodeURIComponent(saveName)}`);
        } catch {
            return null;
        }
    },

    async downloadProjectApp(saveName, projectName) {
        try {
            console.warn('Этот метод устарел. Используйте downloadAppConfig(savingId)');
            return request(`/jarch/download-config-file/${encodeURIComponent(projectName)}/${encodeURIComponent(saveName)}/`);
        } catch {
            return null;
        }
    },

    async hasProjectSaves(projectId) {
        try {
            const saves = await this.getProjectSaves(projectId);
            return Array.isArray(saves) && saves.length > 0;
        } catch {
            return false;
        }
    }
};