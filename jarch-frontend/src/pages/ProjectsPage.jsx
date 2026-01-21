import React, { useState, useEffect } from 'react';
import { projectService } from '../services/projectService';
import { teamService } from '../services/teamService';
import { saveService } from '../services/saveService';
import ProjectList from '../components/ProjectList';
import TeamSection from '../components/TeamSection';
import ConfigBuilderSection from '../components/ConfigBuilderSection';
import CreateProjectForm from '../components/CreateProjectForm';
import SectionTabs from '../components/SectionTabs';

const ProjectsPage = () => {
    const [ownedProjects, setOwnedProjects] = useState([]);
    const [joinedProjects, setJoinedProjects] = useState([]);
    const [projectsWithConfigs, setProjectsWithConfigs] = useState({});
    const [loading, setLoading] = useState(false);
    const [selectedProject, setSelectedProject] = useState(null);
    
    const [activeSection, setActiveSection] = useState(() => {
        const savedSection = localStorage.getItem('projectsPageActiveSection');
        return savedSection || 'projects';
    });
    
    const [projectSaves, setProjectSaves] = useState([]);
    const [members, setMembers] = useState([]);
    
    const [appConfig, setAppConfig] = useState(null);
    const [entityConfig, setEntityConfig] = useState(null);
    const [appConfigValid, setAppConfigValid] = useState(false);
    const [entityConfigValid, setEntityConfigValid] = useState(false);

    const [selectedProjectId, setSelectedProjectId] = useState(() => {
        const savedProjectId = localStorage.getItem('projectsPageSelectedProjectId');
        return savedProjectId ? parseInt(savedProjectId) : null;
    });

    useEffect(() => {
        loadAllProjects();
    }, []);
    
    useEffect(() => {
        if (selectedProject) {
            setSelectedProjectId(selectedProject.id);
        }
    }, [selectedProject]);

    useEffect(() => {
        if ((ownedProjects.length > 0 || joinedProjects.length > 0) && selectedProjectId) {
            const allProjects = [...ownedProjects, ...joinedProjects];
            const projectToSelect = allProjects.find(project => project.id === selectedProjectId);
            if (projectToSelect) {
                setSelectedProject(projectToSelect);
            }
        }
    }, [ownedProjects, joinedProjects, selectedProjectId]);

    useEffect(() => {
        localStorage.setItem('projectsPageActiveSection', activeSection);
    }, [activeSection]);

    useEffect(() => {
        if (selectedProjectId) {
            localStorage.setItem('projectsPageSelectedProjectId', selectedProjectId.toString());
        } else {
            localStorage.removeItem('projectsPageSelectedProjectId');
        }
    }, [selectedProjectId]);
    
    useEffect(() => {
        if (selectedProject && activeSection === 'team') {
            loadTeamMembers();
        }
        if (selectedProject && activeSection === 'config-builder') {
            loadProjectSaves();
        }
    }, [selectedProject, activeSection]);
    
    const loadAllProjects = async () => {
        setLoading(true);
        
        try {
            const [owned, joined] = await Promise.all([
                projectService.getUserProjects(),
                projectService.getJoinedProjects()
            ]);
            
            setOwnedProjects(Array.isArray(owned) ? owned : []);
            setJoinedProjects(Array.isArray(joined) ? joined : []);
            
            const allProjects = [...(Array.isArray(owned) ? owned : []), ...(Array.isArray(joined) ? joined : [])];
            const configsPromises = allProjects.map(async (project) => {
                try {
                    const saves = await saveService.getProjectSaves(project.id);
                    const hasConfigs = Array.isArray(saves) && saves.length > 0;
                    return { projectId: project.id, hasConfigs };
                } catch (error) {
                    return { projectId: project.id, hasConfigs: false };
                }
            });
            
            const configsResults = await Promise.all(configsPromises);
            const configsMap = {};
            configsResults.forEach(result => {
                configsMap[result.projectId] = result.hasConfigs;
            });
            
            setProjectsWithConfigs(configsMap);
        } catch (err) {
        } finally {
            setLoading(false);
        }
    };
    
    const loadTeamMembers = async () => {
        if (!selectedProject) return;
        
        try {
            const teamMembers = await teamService.getTeamMembers(selectedProject.id);
            setMembers(Array.isArray(teamMembers) ? teamMembers : []);
        } catch (err) {
            setMembers([]);
        }
    };
    
    const loadProjectSaves = async () => {
        if (!selectedProject) return;
        
        try {
            const saves = await saveService.getProjectSaves(selectedProject.id);
            setProjectSaves(Array.isArray(saves) ? saves : []);
            
            setProjectsWithConfigs(prev => ({
                ...prev,
                [selectedProject.id]: (Array.isArray(saves) && saves.length > 0)
            }));
        } catch (err) {
            setProjectSaves([]);
        }
    };
    
    const handleProjectCreated = () => {
        loadAllProjects();
    };

    const handleConfigSaved = () => {
        loadProjectSaves();
    };

    const areConfigsValid = appConfigValid && entityConfigValid;

    const isProjectOwner = selectedProject ? 
        ownedProjects.some(project => project.id === selectedProject.id) : 
        false;

    return (
        <div className="projects-page">
            <h2>Мои проекты</h2>
            
            <SectionTabs 
                activeSection={activeSection}
                selectedProject={selectedProject}
                onSectionChange={setActiveSection}
            />
            
            {activeSection === 'projects' && (
                <div className="projects-content">
                    <CreateProjectForm 
                        onProjectCreated={handleProjectCreated}
                        loading={loading}
                    />
                    <ProjectList 
                        ownedProjects={ownedProjects}
                        joinedProjects={joinedProjects}
                        projectsWithConfigs={projectsWithConfigs}
                        selectedProject={selectedProject}
                        onProjectSelect={(project) => {
                            setSelectedProject(project);
                            setSelectedProjectId(project?.id || null);
                        }}
                    />
                </div>
            )}
            
            {activeSection === 'team' && (
                <TeamSection 
                    selectedProject={selectedProject}
                    loading={loading}
                />
            )}
            
            {activeSection === 'config-builder' && (
                <ConfigBuilderSection 
                    selectedProject={selectedProject}
                    projectSaves={projectSaves}
                    areConfigsValid={areConfigsValid}
                    loading={loading}
                    isProjectOwner={isProjectOwner}
                    onConfigSaved={handleConfigSaved}
                    onAppConfigChange={setAppConfig}
                    onEntityConfigChange={setEntityConfig}
                    onAppConfigValidationChange={setAppConfigValid}
                    onEntityConfigValidationChange={setEntityConfigValid}
                />
            )}
        </div>
    );
};

export default ProjectsPage;