import React, { useState, useEffect } from 'react';
import { projectService } from '../services/projectService';
import { teamService } from '../services/teamService';
import { saveService } from '../services/saveService';
import ProjectList from '../components/ProjectList';
import TeamSection from '../components/TeamSection';
import ConfigBuilderSection from '../components/ConfigBuilderSection';
import CreateProjectForm from '../components/CreateProjectForm';
import SectionTabs from '../components/SectionTabs';
import StatusMessage from '../components/StatusMessage';

const ProjectsPage = () => {
    const [ownedProjects, setOwnedProjects] = useState([]);
    const [joinedProjects, setJoinedProjects] = useState([]);
    const [projectsWithConfigs, setProjectsWithConfigs] = useState({});
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [selectedProject, setSelectedProject] = useState(null);
    const [activeSection, setActiveSection] = useState('projects');
    const [projectSaves, setProjectSaves] = useState([]);
    const [members, setMembers] = useState([]);
    
    // Добавляем состояния для конфигураций
    const [appConfig, setAppConfig] = useState(null);
    const [entityConfig, setEntityConfig] = useState(null);
    const [appConfigValid, setAppConfigValid] = useState(false);
    const [entityConfigValid, setEntityConfigValid] = useState(false);

    useEffect(() => {
        loadAllProjects();
    }, []);
    
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
        setError('');
        setSuccess('');
        
        try {
            const [owned, joined] = await Promise.all([
                projectService.getUserProjects(),
                projectService.getJoinedProjects()
            ]);
            
            setOwnedProjects(Array.isArray(owned) ? owned : []);
            setJoinedProjects(Array.isArray(joined) ? joined : []);
            
            // Проверяем наличие конфигураций
            const allProjects = [...(Array.isArray(owned) ? owned : []), ...(Array.isArray(joined) ? joined : [])];
            const configsPromises = allProjects.map(async (project) => {
                try {
                    const saves = await saveService.getProjectSaves(project.id);
                    return { projectId: project.id, hasConfigs: Array.isArray(saves) && saves.length > 0 };
                } catch {
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
            console.error('Ошибка загрузки проектов:', err);
            setError('Ошибка загрузки проектов: ' + err.message);
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
            console.error('Ошибка загрузки команды:', err);
            setMembers([]);
        }
    };
    
    const loadProjectSaves = async () => {
        if (!selectedProject) return;
        
        try {
            const saves = await saveService.getProjectSaves(selectedProject.id);
            setProjectSaves(Array.isArray(saves) ? saves : []);
            
            // Обновляем статус наличия конфигураций
            setProjectsWithConfigs(prev => ({
                ...prev,
                [selectedProject.id]: (Array.isArray(saves) && saves.length > 0)
            }));
        } catch (err) {
            console.error('Ошибка загрузки сохранений:', err);
            setProjectSaves([]);
        }
    };
    
    const handleProjectCreated = () => {
        setSuccess('Проект успешно создан');
        loadAllProjects();
    };

    const handleMemberAdded = (username) => {
        setSuccess(`Пользователь ${username} добавлен в проект`);
        loadTeamMembers();
    };

    const handleMemberRemoved = (username) => {
        setSuccess(`Участник ${username} удален из проекта`);
        loadTeamMembers();
    };

    const handleConfigSaved = () => {
        setSuccess(`Конфигурации сохранены в проект "${selectedProject?.name}"`);
        loadProjectSaves();
    };

    const handleSaveDeleted = () => {
        setSuccess('Сохранение удалено');
        loadProjectSaves();
    };

    const areConfigsValid = appConfigValid && entityConfigValid;

    return (
        <div className="projects-page">
            <h2>Мои проекты</h2>
            
            <StatusMessage error={error} success={success} />
            
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
                        onProjectSelect={setSelectedProject}
                    />
                </div>
            )}
            
            {activeSection === 'team' && (
                <TeamSection 
                    selectedProject={selectedProject}
                    members={members}
                    loading={loading}
                    onMemberAdded={handleMemberAdded}
                    onMemberRemoved={handleMemberRemoved}
                    onReloadMembers={loadTeamMembers}
                />
            )}
            
            {activeSection === 'config-builder' && (
                <ConfigBuilderSection 
                    selectedProject={selectedProject}
                    projectSaves={projectSaves}
                    areConfigsValid={areConfigsValid}
                    loading={loading}
                    onConfigSaved={handleConfigSaved}
                    onConfigDownloaded={() => setSuccess('Конфигурации скачаны')}
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