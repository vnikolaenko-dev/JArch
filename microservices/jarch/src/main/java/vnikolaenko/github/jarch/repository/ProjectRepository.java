package vnikolaenko.github.jarch.repository;

import vnikolaenko.github.jarch.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vnikolaenko.github.jarch.model.TeamMember;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByOwner(String username);

    Project findProjectByNameAndOwner(String projectName, String username);

    List<Project> findAllByTeamMembersContaining(TeamMember user);
}
