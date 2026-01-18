package vnikolaenko.github.jarch.service;

import vnikolaenko.github.jarch.model.Project;
import vnikolaenko.github.jarch.model.TeamMember;
import vnikolaenko.github.jarch.repository.ProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vnikolaenko.github.jarch.repository.TeamMemberRepository;
import vnikolaenko.github.jarch.utils.SecurityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectAccessService projectAccessService;
    private final SecurityUtils securityUtils;
    private final UserService userService;

    public List<Project> getAllUserProjects(String username) {
        return projectRepository.findAllByOwner(username);
    }

    public Project getUserProject(String username, String projectName) {
        return projectRepository.findProjectByNameAndOwner(projectName, username);
    }

    public void save(Project project) {
        String username = securityUtils.getCurrentUsername();
        project.setOwner(username);
        projectRepository.save(project);
    }

    public void update(Long id, Project project) {
        Optional<Project> projectOptional = projectRepository.findById(id);
        if (projectOptional.isEmpty()) {
            throw new RuntimeException("Project with id " + id + " already exists");
        }
        if (projectAccessService.hasAccessToProject(id)){
            projectRepository.save(project);
            return;
        }
        throw new RuntimeException("You " + id + " doesn't have access to project");
    }

    public List<Project> getAllUserJoinedProjects(String username) {
        String userEmail = userService.getUserEmail(username);
        Optional<TeamMember> user = teamMemberRepository.findByUsername(userEmail);

        if (user.isEmpty()) {
            return new ArrayList<>();
        }

        return projectRepository.findAllByTeamMembersContaining(user.get());
    }


    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId).orElse(null);
    }
}
