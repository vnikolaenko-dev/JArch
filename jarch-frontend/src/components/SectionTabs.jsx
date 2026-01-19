import React from 'react';

const SectionTabs = ({ activeSection, selectedProject, onSectionChange }) => {
    return (
        <div className="section-tabs">
            <button 
                onClick={() => onSectionChange('projects')}
                className={`section-tab ${activeSection === 'projects' ? 'active' : ''}`}
            >
                Проекты
            </button>
            <button 
                onClick={() => onSectionChange('team')}
                className={`section-tab ${activeSection === 'team' ? 'active' : ''} ${!selectedProject ? 'disabled' : ''}`}
                disabled={!selectedProject}
            >
                Команда
            </button>
            <button 
                onClick={() => onSectionChange('config-builder')}
                className={`section-tab ${activeSection === 'config-builder' ? 'active' : ''}`}
            >
                Конструктор
            </button>
        </div>
    );
};

export default SectionTabs;