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

    public List<TeamMember> findAllByProjectId(long projectId) {
        return teamMemberRepository.findAllByProject_Id(projectId);
    }

    public void save(TeamMember teamMember, long projectId, String username) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            throw new RuntimeException("Project not found");
        }
        if (!project.get().getOwner().equals(username)) {
            throw new RuntimeException("You are not the owner of this project");
        }
        teamMember.setProject(project.get());
        teamMemberRepository.save(teamMember);
    }

    @Transactional
    public void delete(String memberUsername, long projectId, String username) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            throw new RuntimeException("Project not found");
        }
        if (!project.get().getOwner().equals(username)) {
            throw new RuntimeException("You are not the owner of this project");
        }
        teamMemberRepository.deleteTeamMemberByUsernameAndProject_Id(memberUsername, projectId);
    }
}
