package vnikolaenko.github.jarch.service;

import vnikolaenko.github.jarch.model.Saving;
import vnikolaenko.github.jarch.repository.SavingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SavingService {
    private SavingRepository savingRepository;

    public void save(Saving saving) {
        savingRepository.save(saving);
    }

    public List<Saving> getSavingsByProject(Long projectId) {
        return savingRepository.findByProjectId(projectId);
    }

    public Optional<Saving> getSavingById(Long id) {
        return savingRepository.findById(id);
    }
}
