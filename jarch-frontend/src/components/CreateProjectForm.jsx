import React, { useState } from 'react';
import { projectService } from '../services/projectService';

const CreateProjectForm = ({ onProjectCreated, loading }) => {
    const [showForm, setShowForm] = useState(false);
    const [projectName, setProjectName] = useState('');
    const [projectDescription, setProjectDescription] = useState('');
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await projectService.createProject({
                name: projectName,
                description: projectDescription || '',
            });
            
            setProjectName('');
            setProjectDescription('');
            setError('');
            setShowForm(false);
            onProjectCreated();
        } catch (error) {
            setError(error.message || 'Ошибка создания проекта');
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
        }
    };

    return (
        <div className="create-project-section">
            <button 
                type="button"
                onClick={() => setShowForm(!showForm)}
                className="toggle-form-button"
                disabled={loading}
            >
                {showForm ? '[Скрыть форму создания]' : '[Создать новый проект]'}
            </button>
            
            {showForm && (
                <form onSubmit={handleSubmit} onKeyDown={handleKeyDown} className="project-form">
                    <div className="form-group">
                        <label>Название проекта:</label>
                        <input 
                            type="text" 
                            value={projectName}
                            onChange={(e) => {
                                setProjectName(e.target.value);
                                setError('');
                            }}
                            placeholder="Введите название проекта" 
                            required 
                            disabled={loading}
                        />
                    </div>
                    <div className="form-group">
                        <label>Описание:</label>
                        <textarea 
                            value={projectDescription}
                            onChange={(e) => {
                                setProjectDescription(e.target.value);
                                setError('');
                            }}
                            placeholder="Описание проекта" 
                            rows="3"
                            disabled={loading}
                        />
                    </div>
                    
                    {error && (
                        <div className="error-message">
                            {error}
                        </div>
                    )}
                    
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