package vnikolaenko.github.jarch.repository;

import vnikolaenko.github.jarch.model.Saving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingRepository extends JpaRepository<Saving, Long> {
    List<Saving> findAllByProject_Id(long project_id);

    List<Saving> findByProjectId(Long projectId);
}
