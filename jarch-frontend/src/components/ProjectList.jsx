import React from 'react';
import ProjectCard from './ProjectCard';

const ProjectList = ({ 
    ownedProjects, 
    joinedProjects, 
    projectsWithConfigs, 
    selectedProject, 
    onProjectSelect 
}) => {
    const hasOwnedProjects = ownedProjects.length > 0;
    const hasJoinedProjects = joinedProjects.length > 0;
    const hasAnyProjects = hasOwnedProjects || hasJoinedProjects;

    if (!hasAnyProjects) {
        return (
            <div className="no-projects">
                У вас еще нет проектов. Создайте первый проект!
            </div>
        );
    }

    return (
        <div className="projects-list-section">
            {hasOwnedProjects && (
                <div className="projects-section">
                    <h3>Мои проекты (владелец)</h3>
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
                    </div>
                </div>
            )}
            
            {hasJoinedProjects && (
                <div className="projects-section">
                    <h3>Присоединенные проекты</h3>
                    <div className="projects-grid">
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
            )}
        </div>
    );
};

export default ProjectList;