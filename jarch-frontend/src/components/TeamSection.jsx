import React, { useState, useEffect } from 'react';
import { teamService } from '../services/teamService';
import { getUsernameFromToken } from '../utils/jwtUtils';

const TeamSection = ({ 
    selectedProject, 
    loading
}) => {
    const [members, setMembers] = useState([]);
    const [memberUsername, setMemberUsername] = useState('');
    const [localLoading, setLocalLoading] = useState(false);
    const [error, setError] = useState('');
    const [isOwner, setIsOwner] = useState(false);

    useEffect(() => {
        if (selectedProject) {
            loadTeamMembers();
            checkIfOwner();
        }
    }, [selectedProject]);

    const checkIfOwner = () => {
        const currentUsername = getUsernameFromToken();
        setIsOwner(selectedProject && selectedProject.owner === currentUsername);
    };

    const loadTeamMembers = async () => {
        if (!selectedProject) return;
        
        try {
            const teamMembers = await teamService.getTeamMembers(selectedProject.id);
            setMembers(Array.isArray(teamMembers) ? teamMembers : []);
            setError('');
        } catch (error) {
            setMembers([]);
            setError('Ошибка загрузки участников');
        }
    };

    const handleAddMember = async (e) => {
        e.preventDefault();
        
        if (!memberUsername.trim()) {
            setError('Введите имя пользователя');
            return;
        }

        setLocalLoading(true);
        setError('');
        try {
            await teamService.addMember(selectedProject.id, {
                username: memberUsername
            });
            
            await loadTeamMembers();
            
            setMemberUsername('');
        } catch (error) {
            setError(error.message || 'Ошибка добавления участника');
        } finally {
            setLocalLoading(false);
        }
    };

    const handleRemoveMember = async (username) => {
        setLocalLoading(true);
        setError('');
        try {
            await teamService.removeMember(selectedProject.id, username);
            
            await loadTeamMembers();
            
        } catch (error) {
            setError(error.message || 'Ошибка удаления участника');
        } finally {
            setLocalLoading(false);
        }
    };

    const isLoading = loading || localLoading;

    if (!selectedProject) {
        return (
            <div className="team-content">
                <div className="no-project-selected">
                    <p>Выберите проект чтобы управлять командой</p>
                </div>
            </div>
        );
    }

    return (
        <div className="team-content">
            <div className="selected-project-info">
                <h3>Команда проекта: <span className="project-name">{selectedProject.name}</span></h3>
                {isOwner && <p style={{color: 'var(--color-success)', fontSize: '0.8rem'}}>Вы являетесь владельцем этого проекта</p>}
            </div>
            
            {error && (
                <div className="error-message" style={{color: 'var(--color-error)', fontSize: '0.8rem', marginBottom: '10px'}}>
                    {error}
                </div>
            )}
            
            <div className="team-management">
                {isOwner && (
                    <div className="add-member-section">
                        <form onSubmit={handleAddMember} className="member-form">
                            <div className="form-group">
                                <label>Добавить участника:</label>
                                <input 
                                    type="text" 
                                    value={memberUsername}
                                    onChange={(e) => {
                                        setMemberUsername(e.target.value);
                                        setError('');
                                    }}
                                    placeholder="Имя пользователя" 
                                    required 
                                    disabled={isLoading}
                                />
                            </div>
                            <button 
                                type="submit" 
                                disabled={isLoading}
                                className="submit-button"
                            >
                                {isLoading ? '[Добавление...]' : '[Добавить в команду]'}
                            </button>
                        </form>
                    </div>
                )}
                
                <div className="members-list-section" style={{ 
                    width: isOwner ? '' : '100%',
                    gridColumn: isOwner ? '2' : '1 / span 2'
                }}>
                    <h4>Участники команды {!isOwner && '(только просмотр)'}</h4>
                    
                    <div className="members-list">
                        {members.length === 0 ? (
                            <p className="no-members">Нет участников в этом проекте</p>
                        ) : (
                            <>
                                <div key="owner" className="member-item owner-item">
                                    <div className="member-info">
                                        <div className="member-username">{selectedProject.owner} (владелец)</div>
                                        <div className="member-role">Роль: OWNER</div>
                                    </div>
                                    <span className="owner-badge">Владелец</span>
                                </div>
                                
                                {members.map(member => (
                                    <div key={member.id || member.username} className="member-item">
                                        <div className="member-info">
                                            <div className="member-username">{member.username}</div>
                                            <div className="member-role">Роль: {member.root || 'VIEWER'}</div>
                                        </div>
                                        {isOwner && member.username !== selectedProject.owner && (
                                            <button 
                                                onClick={() => handleRemoveMember(member.username)}
                                                disabled={isLoading}
                                                className="remove-button"
                                            >
                                                Удалить
                                            </button>
                                        )}
                                    </div>
                                ))}
                            </>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TeamSection;