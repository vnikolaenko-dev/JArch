package vnikolaenko.github.jarch.repository;

import vnikolaenko.github.jarch.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findAllByProject_Id(long project_id);

    void deleteTeamMemberByUsernameAndProject_Id(String memberUsername, long projectId);

    Optional<TeamMember> findByUsername(String userEmail);

    boolean existsByUsernameAndProject_Id(String username, long projectId);
}
