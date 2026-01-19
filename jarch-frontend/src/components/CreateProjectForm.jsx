import React, { useState } from 'react';
import { projectService } from '../services/projectService';

const CreateProjectForm = ({ onProjectCreated, loading }) => {
    const [showForm, setShowForm] = useState(false);
    const [projectName, setProjectName] = useState('');
    const [projectDescription, setProjectDescription] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!projectName.trim()) return;

        try {
            await projectService.createProject({
                name: projectName,
                description: projectDescription || '',
            });
            
            setProjectName('');
            setProjectDescription('');
            setShowForm(false);
            onProjectCreated();
        } catch {}
    };

    return (
        <div className="create-project-section">
            <button 
                onClick={() => setShowForm(!showForm)}
                className="toggle-form-button"
                disabled={loading}
            >
                {showForm ? '[Скрыть форму создания]' : '[Создать новый проект]'}
            </button>
            
            {showForm && (
                <form onSubmit={handleSubmit} className="project-form">
                    <div className="form-group">
                        <label>Название проекта:</label>
                        <input 
                            type="text" 
                            value={projectName}
                            onChange={(e) => setProjectName(e.target.value)}
                            placeholder="Введите название проекта" 
                            required 
                            disabled={loading}
                        />
                    </div>
                    <div className="form-group">
                        <label>Описание:</label>
                        <textarea 
                            value={projectDescription}
                            onChange={(e) => setProjectDescription(e.target.value)}
                            placeholder="Описание проекта" 
                            rows="3"
                            disabled={loading}
                        />
                    </div>
                    <button 
                        type="submit"
                        disabled={loading}
                        className="submit-button"
                    >
                        {loading ? '[Создание...]' : '[Создать проект]'}
                    </button>
                </form>
            )}
        </div>
    );
};

export default CreateProjectForm;