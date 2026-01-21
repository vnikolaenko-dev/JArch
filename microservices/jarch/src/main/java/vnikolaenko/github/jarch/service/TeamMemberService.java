package vnikolaenko.github.jarch.service;

import org.springframework.transaction.annotation.Transactional;
import vnikolaenko.github.jarch.model.Project;
import vnikolaenko.github.jarch.model.TeamMember;
import vnikolaenko.github.jarch.repository.ProjectRepository;
import vnikolaenko.github.jarch.repository.TeamMemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TeamMemberService {
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserService userService;

    public List<TeamMember> findAllByProjectId(long projectId) {
        return teamMemberRepository.findAllByProject_Id(projectId);
    }

    public void save(TeamMember teamMember, long projectId, String username) {
        Optional<Project> project = projectRepository.findById(projectId);

        if (project.isEmpty()) {
            throw new RuntimeException("Проект не найден");
        }
        
        Project projectEntity = project.get();
        
        if (!projectEntity.getOwner().equals(username)) {
            throw new RuntimeException("Вы не являетесь владельцем этого проекта");
        }
        
        if (teamMember.getUsername().equals(username)) {
            throw new RuntimeException("Нельзя добавить самого себя в участники");
        }
        
        boolean alreadyMember = teamMemberRepository.findAllByProject_Id(projectId).stream()
                .anyMatch(tm -> tm.getUsername().equals(teamMember.getUsername()));
        
        if (alreadyMember) {
            throw new RuntimeException("Пользователь уже добавлен в проект");
        }
        
        teamMember.setProject(projectEntity);
        teamMemberRepository.save(teamMember);
    }

    @Transactional
    public void delete(String memberUsername, long projectId, String username) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            throw new RuntimeException("Проект не найден");
        }
        
        Project projectEntity = project.get();
        
        if (!projectEntity.getOwner().equals(username)) {
            throw new RuntimeException("Вы не являетесь владельцем этого проекта");
        }
        
        if (memberUsername.equals(projectEntity.getOwner())) {
            throw new RuntimeException("Нельзя удалить владельца проекта");
        }
        
        boolean memberExists = teamMemberRepository.findAllByProject_Id(projectId).stream()
                .anyMatch(tm -> tm.getUsername().equals(memberUsername));
        
        if (!memberExists) {
            throw new RuntimeException("Участник не найден в проекте");
        }
        
        teamMemberRepository.deleteTeamMemberByUsernameAndProject_Id(memberUsername, projectId);
    }
}