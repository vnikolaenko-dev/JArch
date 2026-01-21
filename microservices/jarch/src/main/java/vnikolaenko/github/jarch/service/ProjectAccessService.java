package vnikolaenko.github.jarch.service;

import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vnikolaenko.github.jarch.model.Project;
import vnikolaenko.github.jarch.repository.ProjectRepository;
import vnikolaenko.github.jarch.repository.TeamMemberRepository;
import vnikolaenko.github.jarch.utils.SecurityUtils;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ProjectAccessService {

    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SecurityUtils securityUtils;

    public boolean hasAccessToProject(Long projectId) {
        String currentUsername = securityUtils.getCurrentUsername();
        
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            return false;
        }

        Project projectEntity = project.get();

        if (currentUsername.equals(projectEntity.getOwner())) {
            return true;
        }

        return teamMemberRepository.findAllByProject_Id(projectId).stream()
                .anyMatch(teamMember -> teamMember.getUsername().equals(currentUsername));
    }

    public void validateProjectAccess(Long projectId) {
        if (!hasAccessToProject(projectId)) {
            throw new AccessDeniedException("No access to project");
        }
    }

    public Project getProjectWithAccessCheck(Long projectId) {
        validateProjectAccess(projectId);
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }
}