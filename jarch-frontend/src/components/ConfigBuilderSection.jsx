import React, { useState, useEffect } from 'react';
import AppConfigBuilderPage from '../pages/AppConfigBuilderPage';
import { saveService } from '../services/saveService';

const ConfigBuilderSection = ({
    selectedProject,
    projectSaves,
    areConfigsValid,
    loading,
    isProjectOwner,
    onConfigSaved,
    onAppConfigChange,
    onEntityConfigChange,
    onAppConfigValidationChange,
    onEntityConfigValidationChange
}) => {
    const [saveName, setSaveName] = useState('');
    const [appConfig, setAppConfig] = useState(null);
    const [entityConfig, setEntityConfig] = useState(null);
    const [loadingConfig, setLoadingConfig] = useState(false);
    const [editingSaveId, setEditingSaveId] = useState(null);
    const [currentSaveName, setCurrentSaveName] = useState('');
    const [editorKey, setEditorKey] = useState(Date.now());

    const handleSaveToProject = async () => {
        if (!selectedProject || !areConfigsValid || !saveName.trim()) {
            return;
        }

        try {
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

            if (editingSaveId) {
                await saveService.updateSave(
                    editingSaveId,
                    saveName,
                    entityConfigFile,
                    appConfigFile
                );
            } else {
                await saveService.createSave(
                    selectedProject.id, 
                    saveName, 
                    entityConfigFile, 
                    appConfigFile
                );
            }
            
            onConfigSaved();
            resetForm();
        } catch (error) {
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
    };

    const handleDownloadFromProject = async (save) => {
        if (!selectedProject) return;

        try {
            const savingId = save.id;
            
            const [entityBlob, appBlob] = await Promise.all([
                saveService.downloadEntityConfig(savingId),
                saveService.downloadAppConfig(savingId)
            ]);
            
            const entityUrl = URL.createObjectURL(entityBlob);
            const appUrl = URL.createObjectURL(appBlob);
            
            const entityLink = document.createElement('a');
            entityLink.href = entityUrl;
            entityLink.download = `${save.name}_entity.json`;
            
            const appLink = document.createElement('a');
            appLink.href = appUrl;
            appLink.download = `${save.name}_app.json`;
            
            entityLink.click();
            setTimeout(() => {
                appLink.click();
                
                setTimeout(() => {
                    URL.revokeObjectURL(entityUrl);
                    URL.revokeObjectURL(appUrl);
                }, 100);
            }, 100);
            
        } catch (error) {
        }
    };

    const handleDeleteSave = async (save) => {
        if (!selectedProject) {
            return;
        }

        try {
            const savingId = save.id;
            await saveService.deleteSave(savingId);
            onConfigSaved();
            if (editingSaveId === savingId) {
                resetForm();
            }
        } catch (error) {
        }
    };

    const loadConfigForView = async (save) => {
        if (!selectedProject) return;
        
        setLoadingConfig(true);
        try {
            const savingId = save.id;
            const configData = await saveService.getSavingConfig(savingId);
            
            if (!configData) {
                throw new Error('Конфигурация не найдена');
            }
            
            let entityData;
            let appData;
            
            try {
                entityData = typeof configData.entityConfig === 'string' 
                    ? JSON.parse(configData.entityConfig) 
                    : configData.entityConfig;
                appData = typeof configData.appConfig === 'string'
                    ? JSON.parse(configData.appConfig)
                    : configData.appConfig;
            } catch (parseError) {
                throw new Error('Неверный формат конфигурационных файлов');
            }
            
            setEntityConfig(entityData);
            setAppConfig(appData);
            
            if (onEntityConfigChange) onEntityConfigChange(entityData);
            if (onAppConfigChange) onAppConfigChange(appData);
            
            if (onEntityConfigValidationChange) onEntityConfigValidationChange(true);
            if (onAppConfigValidationChange) onAppConfigValidationChange(true);
            
            setCurrentSaveName(configData.saving.name);
            setSaveName(configData.saving.name);
            setEditingSaveId(savingId);
            
            setEditorKey(Date.now());
            
        } catch (error) {
        } finally {
            setLoadingConfig(false);
        }
    };

    const resetForm = () => {
        setSaveName('');
        setEditingSaveId(null);
        setCurrentSaveName('');
        setAppConfig(null);
        setEntityConfig(null);
        setEditorKey(Date.now());
        
        if (onEntityConfigValidationChange) onEntityConfigValidationChange(false);
        if (onAppConfigValidationChange) onAppConfigValidationChange(false);
    };

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

    useEffect(() => {
        if (selectedProject) {
            resetForm();
        }
    }, [selectedProject]);

    const cancelEditing = () => {
        resetForm();
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
                        <div className="project-role-badge">
                            {isProjectOwner ? 'Владелец' : 'Участник'}
                        </div>
                        {editingSaveId && (
                            <div className="editing-notice">
                                {isProjectOwner ? 'Редактирование' : 'Просмотр'} сохранения: {currentSaveName}
                            </div>
                        )}
                    </div>

                    <div className="save-controls">
                        <h4>Сохранение конфигураций</h4>
                        <div className="save-form">
                            <input
                                type="text"
                                value={saveName}
                                onChange={(e) => setSaveName(e.target.value)}
                                placeholder={editingSaveId ? currentSaveName : "Название сохранения"}
                                className="save-input"
                                disabled={!isProjectOwner && editingSaveId}
                            />
                            <div className="action-buttons">
                                <button
                                    onClick={handleDownloadConfig}
                                    disabled={!areConfigsValid || loading}
                                    className="download-button"
                                >
                                    Скачать конфигурации
                                </button>

                                {isProjectOwner && (
                                    <button
                                        onClick={handleSaveToProject}
                                        disabled={!areConfigsValid || !saveName.trim() || loading}
                                        className="save-project-button"
                                    >
                                        {editingSaveId ? 'Обновить сохранение' : 'Сохранить в проект'}
                                    </button>
                                )}
                                
                                {editingSaveId && (
                                    <button
                                        onClick={cancelEditing}
                                        className="cancel-edit-button"
                                    >
                                        Отмена
                                    </button>
                                )}
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
                                            <span className="save-name">{save.name}</span>
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
                                            {isProjectOwner ? (
                                                <>
                                                    <button 
                                                        onClick={() => loadConfigForView(save)}
                                                        disabled={loading || loadingConfig}
                                                        className={editingSaveId === save.id ? "edit-save-button active" : "edit-save-button"}
                                                    >
                                                        {editingSaveId === save.id ? 'Выбрано' : 'Редактировать'}
                                                    </button>
                                                    <button 
                                                        onClick={() => handleDeleteSave(save)}
                                                        disabled={loading || save.id === editingSaveId}
                                                        className="delete-save-button"
                                                    >
                                                        Удалить
                                                    </button>
                                                </>
                                            ) : (
                                                <button 
                                                    onClick={() => loadConfigForView(save)}
                                                    disabled={loading || loadingConfig}
                                                    className={editingSaveId === save.id ? "view-save-button active" : "view-save-button"}
                                                >
                                                    {editingSaveId === save.id ? 'Просматривается' : 'Просмотреть'}
                                                </button>
                                            )}
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
                key={editorKey} 
                onAppConfigChange={handleAppConfigChange}
                onEntityConfigChange={handleEntityConfigChange}
                onAppConfigValidationChange={handleAppConfigValidationChange}
                onEntityConfigValidationChange={handleEntityConfigValidationChange}
                initialAppConfig={appConfig}
                initialEntityConfig={entityConfig}
            />
        </div>
    );
};

export default ConfigBuilderSection;