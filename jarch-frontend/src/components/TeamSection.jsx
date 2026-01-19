import React, { useState, useEffect } from 'react';
import { teamService } from '../services/teamService';

const TeamSection = ({ 
    selectedProject, 
    loading, 
    onMemberAdded, 
    onMemberRemoved 
}) => {
    const [members, setMembers] = useState([]);
    const [memberUsername, setMemberUsername] = useState('');

    useEffect(() => {
        if (selectedProject) {
            loadTeamMembers();
        }
    }, [selectedProject]);

    const loadTeamMembers = async () => {
        if (!selectedProject) return;
        
        try {
            const teamMembers = await teamService.getTeamMembers(selectedProject.id);
            setMembers(teamMembers);
        } catch {}
    };

    const handleAddMember = async (e) => {
        e.preventDefault();
        
        if (!memberUsername.trim()) return;

        try {
            await teamService.addMember(selectedProject.id, {
                username: memberUsername
            });
            
            setMemberUsername('');
            onMemberAdded(memberUsername);
        } catch {}
    };

    const handleRemoveMember = async (username) => {
        if (!window.confirm(`Удалить участника "${username}" из проекта?`)) return;

        try {
            await teamService.removeMember(selectedProject.id, username);
            onMemberRemoved(username);
        } catch {}
    };

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
            </div>
            
            <div className="team-management">
                <div className="add-member-section">
                    <form onSubmit={handleAddMember} className="member-form">
                        <div className="form-group">
                            <label>Добавить участника:</label>
                            <input 
                                type="text" 
                                value={memberUsername}
                                onChange={(e) => setMemberUsername(e.target.value)}
                                placeholder="Имя пользователя" 
                                required 
                                disabled={loading}
                            />
                        </div>
                        <button 
                            type="submit" 
                            disabled={loading}
                            className="submit-button"
                        >
                            {loading ? '[Добавление...]' : '[Добавить в команду]'}
                        </button>
                    </form>
                </div>
                
                <div className="members-list-section">
                    <h4>Участники команды</h4>
                    
                    <div className="members-list">
                        {members.length === 0 ? (
                            <p className="no-members">Нет участников в этом проекте</p>
                        ) : (
                            members.map(member => (
                                <div key={member.id || member.username} className="member-item">
                                    <div className="member-info">
                                        <div className="member-username">{member.username}</div>
                                        <div className="member-role">Роль: {member.root || 'VIEWER'}</div>
                                    </div>
                                    <button 
                                        onClick={() => handleRemoveMember(member.username)}
                                        disabled={loading}
                                        className="remove-button"
                                    >
                                        Удалить
                                    </button>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TeamSection;