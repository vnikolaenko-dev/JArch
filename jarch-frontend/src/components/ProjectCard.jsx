import React from 'react';

const ProjectCard = ({ project, isOwner, isSelected, hasConfig, onSelect }) => {
    return (
        <div 
            className={`project-card ${isSelected ? 'selected' : ''}`}
            onClick={onSelect}
        >
            <div className="project-card-header">
                <h4 className="project-title">{project.name}</h4>
                <span className={`project-badge ${isOwner ? 'owner' : 'member'}`}>
                    {isOwner ? 'Владелец' : 'Участник'}
                </span>
            </div>
            <p className="project-description">
                {project.description || 'Без описания'}
            </p>
            <div className="project-metadata">
                <div className="project-owner">
                    <small>Владелец: {project.owner}</small>
                </div>
                <div className="project-config-status">
                    {hasConfig ? '✅ Конфигурации' : '❌ Нет конфигураций'}
                </div>
            </div>
        </div>
    );
};

export default ProjectCard;