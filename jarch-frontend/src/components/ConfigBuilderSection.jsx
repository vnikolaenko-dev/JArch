import React, { useState } from 'react';
import AppConfigBuilderPage from '../pages/AppConfigBuilderPage';
import { saveService } from '../services/saveService';

const ConfigBuilderSection = ({
    selectedProject,
    projectSaves,
    areConfigsValid,
    loading,
    onConfigSaved,
    onConfigDownloaded,
    onDownloadFromProject,
    onAppConfigChange,
    onEntityConfigChange,
    onAppConfigValidationChange,
    onEntityConfigValidationChange
}) => {
    const [saveName, setSaveName] = useState('');
    const [appConfig, setAppConfig] = useState(null);
    const [entityConfig, setEntityConfig] = useState(null);

    const handleSaveToProject = async () => {
        if (!selectedProject || !areConfigsValid || !saveName.trim()) {
            alert('Заполните название сохранения и убедитесь, что конфигурации валидны');
            return;
        }

        try {
            // Создаем File объекты из конфигураций
            const appConfigJson = JSON.stringify(appConfig, null, 2);
            const entityConfigJson = JSON.stringify(entityConfig, null, 2);
            
            const appConfigFile = new File(
                [appConfigJson], 
                'app-config.json', 
                { type: 'application/json' }
            );
            
            const entityConfigFile = new File(
                [entityConfigJson], 
                'entity-config.json', 
                { type: 'application/json' }
            );

            // Используем новый метод API
            await saveService.createSave(
                selectedProject.id, 
                saveName, 
                entityConfigFile, 
                appConfigFile
            );
            
            onConfigSaved();
            setSaveName('');
        } catch (error) {
            alert('Ошибка сохранения: ' + error.message);
        }
    };

    const handleDownloadConfig = () => {
        if (!areConfigsValid) return;
        
        let fileName = 'config';
        if (selectedProject) {
            fileName = selectedProject.name;
        }
        
        const timestamp = new Date().toISOString().split('T')[0];
        const downloadName = `${fileName}_${timestamp}`;
        
        if (appConfig) {
            const appBlob = new Blob([JSON.stringify(appConfig, null, 2)], { 
                type: 'application/json' 
            });
            const appUrl = URL.createObjectURL(appBlob);
            const appLink = document.createElement('a');
            appLink.href = appUrl;
            appLink.download = `${downloadName}_app-config.json`;
            appLink.click();
            URL.revokeObjectURL(appUrl);
        }
        
        if (entityConfig) {
            setTimeout(() => {
                const entityBlob = new Blob([JSON.stringify(entityConfig, null, 2)], { 
                    type: 'application/json' 
                });
                const entityUrl = URL.createObjectURL(entityBlob);
                const entityLink = document.createElement('a');
                entityLink.href = entityUrl;
                entityLink.download = `${downloadName}_entity-config.json`;
                entityLink.click();
                URL.revokeObjectURL(entityUrl);
            }, 100);
        }
        
        onConfigDownloaded();
    };

    // Обработчик для скачивания существующего сохранения
    const handleDownloadFromProject = async (save) => {
        if (!selectedProject) return;

        try {
            const savingId = save.id || save;
            
            // Скачиваем оба файла
            const [entityBlob, appBlob] = await Promise.all([
                saveService.downloadEntityConfig(savingId),
                saveService.downloadAppConfig(savingId)
            ]);
            
            // Создаем ссылки для скачивания
            const entityUrl = URL.createObjectURL(entityBlob);
            const appUrl = URL.createObjectURL(appBlob);
            
            const entityLink = document.createElement('a');
            entityLink.href = entityUrl;
            entityLink.download = `${save.name || save}_entity.json`;
            
            const appLink = document.createElement('a');
            appLink.href = appUrl;
            appLink.download = `${save.name || save}_app.json`;
            
            // Скачиваем файлы
            entityLink.click();
            setTimeout(() => {
                appLink.click();
                
                // Очищаем URL
                setTimeout(() => {
                    URL.revokeObjectURL(entityUrl);
                    URL.revokeObjectURL(appUrl);
                }, 100);
            }, 100);
            
        } catch (error) {
            alert('Ошибка скачивания: ' + error.message);
        }
    };

    const handleDeleteSave = async (save) => {
        if (!selectedProject || !window.confirm(`Удалить сохранение "${save.name || save}"?`)) {
            return;
        }

        try {
            const savingId = save.id || save;
            await saveService.deleteSave(savingId);
            // Вызываем onConfigSaved для обновления списка
            onConfigSaved();
        } catch (error) {
            alert('Ошибка удаления: ' + error.message);
        }
    };

    // Обработчики для конфигураций из AppConfigBuilderPage
    const handleAppConfigChange = (config) => {
        setAppConfig(config);
        if (onAppConfigChange) onAppConfigChange(config);
    };

    const handleEntityConfigChange = (config) => {
        setEntityConfig(config);
        if (onEntityConfigChange) onEntityConfigChange(config);
    };

    const handleAppConfigValidationChange = (isValid) => {
        if (onAppConfigValidationChange) onAppConfigValidationChange(isValid);
    };

    const handleEntityConfigValidationChange = (isValid) => {
        if (onEntityConfigValidationChange) onEntityConfigValidationChange(isValid);
    };

    return (
        <div className="config-builder-content">
            {!selectedProject && (
                <div className="no-project-selected">
                    <p>Выберите проект для работы с конфигурациями</p>
                </div>
            )}

            {selectedProject && (
                <>
                    <div className="selected-project-info">
                        <h3>Проект: {selectedProject.name}</h3>
                    </div>

                    <div className="save-controls">
                        <h4>Сохранение конфигураций</h4>
                        <div className="save-form">
                            <input
                                type="text"
                                value={saveName}
                                onChange={(e) => setSaveName(e.target.value)}
                                placeholder="Название сохранения"
                                className="save-input"
                            />
                            <div className="action-buttons">
                                <button
                                    onClick={handleDownloadConfig}
                                    disabled={!areConfigsValid || loading}
                                    className="download-button"
                                >
                                    Скачать конфигурации
                                </button>

                                <button
                                    onClick={handleSaveToProject}
                                    disabled={!areConfigsValid || !saveName.trim() || loading}
                                    className="save-project-button"
                                >
                                    Сохранить в проект
                                </button>
                            </div>
                        </div>
                    </div>

                    {projectSaves && projectSaves.length > 0 && (
                        <div className="project-saves-section">
                            <h4>Сохраненные конфигурации</h4>
                            
                            <div className="saves-list">
                                {projectSaves.map((save, index) => (
                                    <div key={save.id || index} className="save-item">
                                        <div className="save-info">
                                            <span className="save-name">{save.name || save}</span>
                                            {save.createdAt && (
                                                <span className="save-date">
                                                    {new Date(save.createdAt).toLocaleDateString()}
                                                </span>
                                            )}
                                        </div>
                                        <div className="save-actions">
                                            <button 
                                                onClick={() => handleDownloadFromProject(save)}
                                                disabled={loading}
                                                className="download-save-button"
                                            >
                                                Скачать
                                            </button>
                                            <button 
                                                onClick={() => handleDeleteSave(save)}
                                                disabled={loading}
                                                className="delete-save-button"
                                            >
                                                Удалить
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {projectSaves && projectSaves.length === 0 && (
                        <div className="no-saves-message">
                            В этом проекте пока нет сохраненных конфигураций
                        </div>
                    )}
                </>
            )}
            
            <AppConfigBuilderPage 
                onAppConfigChange={handleAppConfigChange}
                onEntityConfigChange={handleEntityConfigChange}
                onAppConfigValidationChange={handleAppConfigValidationChange}
                onEntityConfigValidationChange={handleEntityConfigValidationChange}
            />
        </div>
    );
};

export default ConfigBuilderSection;