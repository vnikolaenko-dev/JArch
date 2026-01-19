import { request, createEventSource, getToken } from './api';

export const projectService = {
    async createProject(projectData) {
        return request('/project/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(projectData)
        });
    },

    async getUserProjects() {
        try {
            const projects = await request('/project/all');
            return Array.isArray(projects) ? projects : [];
        } catch (error) {
            console.error('Ошибка загрузки проектов:', error);
            return [];
        }
    },

    async getJoinedProjects() {
        try {
            const projects = await request('/project/joined');
            return Array.isArray(projects) ? projects : [];
        } catch (error) {
            console.error('Ошибка загрузки проектов:', error);
            return [];
        }
    },

    async getProjectByName(projectName) {
        return request(`/project?projectName=${encodeURIComponent(projectName)}`);
    },

    async generateFromSaving(savingId) {
        return request(`/jarch/generate-project/from-saving/${savingId}`, {
            method: 'POST'
        });
    },

    async downloadProject(id) {
        return request(`/jarch/generate-project/download/${id}`);
    },

    startGenerationStream(id, onLog, onZipReady) {
        const eventSource = createEventSource(`/jarch/generate-project/stream/${id}`);

        eventSource.addEventListener("log", event => {
            try {
                const data = JSON.parse(event.data);
                if (onLog) onLog(data.level, data.message);
            } catch (error) {
                console.error('Ошибка парсинга логов:', error);
            }
        });

        eventSource.addEventListener("zipReady", () => {
            if (onZipReady) onZipReady();
            eventSource.close();
        });

        eventSource.addEventListener("error", (error) => {
            console.error('SSE ошибка:', error);
            if (onLog) onLog('error', 'Ошибка подключения к потоку логов');
            eventSource.close();
        });

        return eventSource;
    }
};