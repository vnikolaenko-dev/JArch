package vnikolaenko.github.jarch.repository;

import vnikolaenko.github.jarch.model.FileType;
import vnikolaenko.github.jarch.model.ProjectFile;
import vnikolaenko.github.jarch.model.Saving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {
    Optional<ProjectFile> findBySavingAndFileType(Saving saving, FileType fileType);
    
    List<ProjectFile> findBySavingId(Long savingId);
    
    void deleteBySavingId(Long savingId);
}
