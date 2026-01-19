package vnikolaenko.github.jarch.service;

import vnikolaenko.github.jarch.model.Saving;
import vnikolaenko.github.jarch.repository.SavingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SavingService {
    private final SavingRepository savingRepository;
    private final ProjectFileService projectFileService;
    
    public Saving save(Saving saving) {
        return savingRepository.save(saving);
    }
    
    public List<Saving> getSavingsByProject(Long projectId) {
        return savingRepository.findByProjectId(projectId);
    }

    public Optional<Saving> getSavingById(Long id) {
        return savingRepository.findById(id);
    }
    @Transactional
    public void deleteSaving(Long id) throws Exception {
        projectFileService.deleteFilesBySaving(id);        
        savingRepository.deleteById(id);
    }
}
