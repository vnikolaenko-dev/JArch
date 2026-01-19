package vnikolaenko.github.jarch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vnikolaenko.github.jarch.model.FileType;
import vnikolaenko.github.jarch.model.Project;
import vnikolaenko.github.jarch.model.ProjectFile;
import vnikolaenko.github.jarch.model.Saving;
import vnikolaenko.github.jarch.repository.ProjectFileRepository;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectFileService {
    
    private final ProjectFileRepository projectFileRepository;
    private final MinioService minioService;
    
    @Transactional
    public void saveConfigFile(
            MultipartFile file,
            Project project,
            Saving saving,
            FileType fileType
    ) throws Exception {
        String storageName = generateStorageName(project, saving, fileType);
        
        minioService.uploadFile(file.getBytes(), storageName);
        
        ProjectFile projectFile = ProjectFile.builder()
                .filename(storageName)
                .fileType(fileType)
                .saving(saving)
                .build();
        
        projectFileRepository.save(projectFile);
    }
    
    public byte[] getFileContent(Long savingId, FileType fileType) throws Exception {
        Saving saving = new Saving();
        saving.setId(savingId);
        
        ProjectFile file = projectFileRepository.findBySavingAndFileType(saving, fileType)
                .orElseThrow(() -> new RuntimeException("File not found for savingId: " + savingId + ", type: " + fileType));
        
        return minioService.downloadFile(file.getFilename());
    }
    
    @Transactional
    public void deleteFilesBySaving(Long savingId) throws Exception {
        List<ProjectFile> files = projectFileRepository.findBySavingId(savingId);
        
        for (ProjectFile file : files) {
            minioService.deleteFile(file.getFilename());
        }
        
        projectFileRepository.deleteBySavingId(savingId);
    }
    
    private String generateStorageName(Project project, Saving saving, FileType fileType) {
        return String.format("project_%d/%s_%s.json",
                project.getId(),
                fileType.name().toLowerCase(),
                UUID.randomUUID().toString()
        );
    }
}