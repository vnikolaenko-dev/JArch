import React, { useState, useEffect, useRef } from 'react';
import { projectService } from '../services/projectService';
import LogViewer from '../components/LogViewer';
import { authService } from '../services/authService';
import { saveService } from '../services/saveService';

const GenerationPage = () => {
    const [logs, setLogs] = useState([]);
    const [isGenerating, setIsGenerating] = useState(false);
    const [selectedProject, setSelectedProject] = useState(null);
    const [ownedProjects, setOwnedProjects] = useState([]);
    const [projectSaves, setProjectSaves] = useState([]);
    const [loadingProjects, setLoadingProjects] = useState(false);
    const [selectedSave, setSelectedSave] = useState(null);
    const [error, setError] = useState('');
    const eventSourceRef = useRef(null);

    useEffect(() => {
        loadProjects();
        return () => {
            if (eventSourceRef.current) {
                eventSourceRef.current.close();
            }
        };
    }, []);

    useEffect(() => {
        if (selectedProject) {
            loadProjectSaves();
        } else {
            setProjectSaves([]);
        }
    }, [selectedProject]);

    const loadProjects = async () => {
        setLoadingProjects(true);
        setError('');
        try {
            const projects = await projectService.getUserProjects();
            setOwnedProjects(Array.isArray(projects) ? projects : []);
        } catch (error) {
            console.error('Ошибка загрузки проектов:', error);
            setError('Ошибка загрузки проектов');
            setOwnedProjects([]);
        } finally {
            setLoadingProjects(false);
        }
    };

    const loadProjectSaves = async () => {
        if (!selectedProject) return;
        
        setError('');
        try {
            const saves = await saveService.getProjectSaves(selectedProject.id);
            setProjectSaves(Array.isArray(saves) ? saves : []);
        } catch (error) {
            console.error('Ошибка загрузки сохранений:', error);
            setError('Ошибка загрузки сохранений');
            setProjectSaves([]);
        }
    };

    const handleProjectConfigSelect = async (save) => {
        if (!save || !save.id) return;
        
        const token = authService.getToken();
        if (!token || !token.trim()) {
            setError('Требуется авторизация');
            alert('Требуется авторизация');
            return;
        }
        
        setSelectedSave(save);
        setError('');
        
        try {
            setIsGenerating(true);
            setLogs([{ level: 'info', message: 'Начинаем генерацию проекта из сохранения...', timestamp: new Date() }]);
            
            // Генерируем проект из сохранения
            const response = await projectService.generateFromSaving(save.id);
            const generationId = response.id || response;
            
            if (eventSourceRef.current) {
                eventSourceRef.current.close();
            }
            
            // Запускаем поток логов
            eventSourceRef.current = projectService.startGenerationStream(
                generationId,
                (level, message) => setLogs(prev => [...prev, { level, message, timestamp: new Date() }]),
                () => handleZipReady(generationId)
            );
        } catch (error) {
            console.error('Ошибка генерации:', error);
            setError('Ошибка генерации: ' + error.message);
            setLogs(prev => [...prev, { 
                level: 'error', 
                message: `Ошибка генерации: ${error.message}`, 
                timestamp: new Date() 
            }]);
            setIsGenerating(false);
        }
    };

    const handleZipReady = async (id) => {
        try {
            const response = await projectService.downloadProject(id);
            
            if (!response.ok) {
                throw new Error('Ошибка загрузки проекта');
            }
            
            const blob = await response.blob();
            const a = document.createElement("a");
            const url = URL.createObjectURL(blob);
            a.href = url;
            a.download = `generated-project-${id}.zip`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            
            setLogs(prev => [...prev, { 
                level: 'success', 
                message: 'Проект успешно сгенерирован и скачан!', 
                timestamp: new Date() 
            }]);
            
            setTimeout(() => URL.revokeObjectURL(url), 100);
        } catch (error) {
            setError('Ошибка скачивания: ' + error.message);
            setLogs(prev => [...prev, { 
                level: 'error', 
                message: `Ошибка скачивания: ${error.message}`, 
                timestamp: new Date() 
            }]);
        } finally {
            setIsGenerating(false);
            if (eventSourceRef.current) {
                eventSourceRef.current.close();
                eventSourceRef.current = null;
            }
        }
    };

    return (
        <div className="generation-page">
            <h2>Генерация проекта</h2>

            {error && (
                <div className="error-message">
                    ⚠️ {error}
                </div>
            )}

            <div className="generation-controls">
                <div className="project-config-section">
                    <div className="project-select">
                        <label>Выберите проект:</label>
                        <select 
                            value={selectedProject?.id || ''}
                            onChange={(e) => {
                                const projectId = e.target.value;
                                const project = ownedProjects.find(p => p.id && p.id.toString() === projectId);
                                setSelectedProject(project || null);
                                setSelectedSave(null);
                            }}
                            disabled={loadingProjects || isGenerating}
                        >
                            <option value="">-- Выберите проект --</option>
                            {ownedProjects.map(project => (
                                <option key={project.id} value={project.id}>
                                    {project.name}
                                </option>
                            ))}
                        </select>
                    </div>
                    
                    {selectedProject && Array.isArray(projectSaves) && projectSaves.length > 0 && (
                        <div className="saves-section">
                            <label>Выберите сохранение:</label>
                            <div className="saves-list">
                                {projectSaves.map((save) => (
                                    <div 
                                        key={save.id} 
                                        className={`save-item ${selectedSave?.id === save.id ? 'selected' : ''}`}
                                        onClick={() => !isGenerating && handleProjectConfigSelect(save)}
                                    >
                                        <span className="save-name">{save.name}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                    
                    {selectedProject && (!Array.isArray(projectSaves) || projectSaves.length === 0) && (
                        <div className="no-saves-message">
                            {loadingProjects ? 'Загрузка...' : 'Нет сохраненных конфигураций для этого проекта'}
                        </div>
                    )}
                </div>

                {selectedSave && (
                    <div className="selected-save-info">
                        Выбрано сохранение: <strong>{selectedSave.name}</strong>
                        {isGenerating && <span> (идет генерация...)</span>}
                    </div>
                )}
            </div>

            <LogViewer logs={logs} />
        </div>
    );
};

export default GenerationPage;