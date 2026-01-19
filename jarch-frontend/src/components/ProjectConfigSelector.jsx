import React from 'react';

const ProjectConfigSelector = ({
    selectedProject,
    ownedProjects,
    projectSaves,
    loadingProjects,
    isGenerating,
    onProjectSelect,
    onConfigSelect,
    onClearFiles
}) => {
    const handleProjectChange = (e) => {
        const projectId = e.target.value;
        const project = ownedProjects.find(p => p.id.toString() === projectId);
        onProjectSelect(project || null);
        onClearFiles();
    };

    return (
        <div className="project-select-section">
            <select 
                value={selectedProject?.id || ''}
                onChange={handleProjectChange}
                disabled={loadingProjects || isGenerating}
            >
                <option value="">-- Выберите проект --</option>
                {ownedProjects.map(project => (
                    <option key={project.id} value={project.id}>
                        {project.name}
                    </option>
                ))}
            </select>
            
            {selectedProject && projectSaves.length > 0 && (
                <div className="project-saves-section">
                    <h4>Доступные конфигурации:</h4>
                    <div className="saves-list">
                        {projectSaves.map((save, index) => (
                            <div key={save.id || index} className="save-item">
                                <span className="save-name">
                                    {save.name}
                                </span>
                                <button 
                                    onClick={() => onConfigSelect(save)}
                                    disabled={isGenerating}
                                    className="action-button"
                                >
                                    [Загрузить]
                                </button>
                            </div>
                        ))}
                    </div>
                </div>
            )}
            
            {selectedProject && projectSaves.length === 0 && (
                <div className="no-saves-message">
                    Нет сохраненных конфигураций для этого проекта
                </div>
            )}
        </div>
    );
};

export default ProjectConfigSelector;