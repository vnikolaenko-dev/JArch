package vnikolaenko.github.jarch.controller.project_manipulation;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vnikolaenko.github.jarch.model.FileType;
import vnikolaenko.github.jarch.model.Project;
import vnikolaenko.github.jarch.model.Saving;
import vnikolaenko.github.jarch.service.*;
import vnikolaenko.github.jarch.utils.SecurityUtils;
import vnikolaenko.github.network.rabbit.UserMessage;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("project-saves")
public class SavingsController {
    private final SecurityUtils securityUtils;
    private final ProjectFileService projectFileService;
    private final ProjectService projectService;
    private final RabbitService rabbitService;
    private final UserService userService;
    private final SavingService savingService;
    private final ProjectAccessService projectAccessService;

    @GetMapping("get-all/{projectId}")
    public ResponseEntity<List<Saving>> getProjectSavings(@PathVariable Long projectId) {
        projectAccessService.validateProjectAccess(projectId);
        List<Saving> savings = savingService.getSavingsByProject(projectId);
        return ResponseEntity.ok(savings);
    }

    @GetMapping("get/{id}")
    public ResponseEntity<Saving> getSaving(@PathVariable Long id) {
        Optional<Saving> saving = savingService.getSavingById(id);
        if (saving.isEmpty()) {
            throw new AccessDeniedException("Saving not found");
        }
        projectAccessService.validateProjectAccess(saving.get().getProject().getId());
        return ResponseEntity.ok(saving.get());
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteSaving(@PathVariable Long id) throws Exception {
        Optional<Saving> saving = savingService.getSavingById(id);
        if (saving.isEmpty()) {
            throw new AccessDeniedException("Saving not found");
        }
        projectAccessService.validateProjectAccess(saving.get().getProject().getId());
        
        savingService.deleteSaving(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> makeSave(
            @RequestParam("saveName") String saveName,
            @RequestParam("projectId") Long projectId,
            @RequestParam("entityConfig") MultipartFile entityConfigFile,
            @RequestParam("appConfig") MultipartFile appConfigFile) throws Exception {
        
        String username = securityUtils.getCurrentUsername();
        Project project = projectService.getProjectById(projectId);
        if (project == null) {
            throw new AccessDeniedException("Project not found");
        }

        Saving saving = new Saving();
        saving.setName(saveName);
        saving.setProject(project);
        saving = savingService.save(saving);

        projectFileService.saveConfigFile(
                entityConfigFile,
                project,
                saving,
                FileType.ENTITY_CONFIG
        );
        
        projectFileService.saveConfigFile(
                appConfigFile,
                project,
                saving,
                FileType.APP_CONFIG
        );

        rabbitService.sendUserMessage(new UserMessage(username, 
                username + " создал новое сохранение под названием " + saveName + ".",
                userService.getUserEmail(username)));
        return ResponseEntity.ok().build();
    }

    @GetMapping("download-entity/{savingId}")
    public ResponseEntity<byte[]> downloadEntityConfig(@PathVariable Long savingId) throws Exception {
        Saving saving = savingService.getSavingById(savingId)
                .orElseThrow(() -> new AccessDeniedException("Saving not found"));
        projectAccessService.validateProjectAccess(saving.getProject().getId());
        
        byte[] content = projectFileService.getFileContent(savingId, FileType.ENTITY_CONFIG);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"entity-config.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(content);
    }

    @GetMapping("download-app/{savingId}")
    public ResponseEntity<byte[]> downloadAppConfig(@PathVariable Long savingId) throws Exception {
        Saving saving = savingService.getSavingById(savingId)
                .orElseThrow(() -> new AccessDeniedException("Saving not found"));
        projectAccessService.validateProjectAccess(saving.getProject().getId());
        
        byte[] content = projectFileService.getFileContent(savingId, FileType.APP_CONFIG);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"app-config.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(content);
    }
}