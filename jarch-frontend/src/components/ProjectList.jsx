import React from 'react';
import ProjectCard from './ProjectCard';

const ProjectList = ({ 
    ownedProjects, 
    joinedProjects, 
    projectsWithConfigs, 
    selectedProject, 
    onProjectSelect 
}) => {
    const allProjects = [...ownedProjects, ...joinedProjects];

    if (allProjects.length === 0) {
        return (
            <div className="no-projects">
                У вас еще нет проектов. Создайте первый проект!
            </div>
        );
    }

    return (
        <div className="projects-list-section">
            <h3>Мои проекты</h3>
            
            <div className="projects-grid">
                {ownedProjects.map(project => {
                    const hasConfig = projectsWithConfigs[project.id] || false;
                    return (
                        <ProjectCard
                            key={project.id}
                            project={project}
                            isOwner={true}
                            isSelected={selectedProject?.id === project.id}
                            hasConfig={hasConfig}
                            onSelect={() => onProjectSelect(project)}
                        />
                    );
                })}
                
                {joinedProjects.map(project => {
                    const hasConfig = projectsWithConfigs[project.id] || false;
                    return (
                        <ProjectCard
                            key={project.id}
                            project={project}
                            isOwner={false}
                            isSelected={selectedProject?.id === project.id}
                            hasConfig={hasConfig}
                            onSelect={() => onProjectSelect(project)}
                        />
                    );
                })}
            </div>
        </div>
    );
};

export default ProjectList;