package vnikolaenko.github.jarch.controller.project_manipulation;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vnikolaenko.github.jarch.model.Project;
import vnikolaenko.github.jarch.model.Saving;
import vnikolaenko.github.jarch.service.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import vnikolaenko.github.jarch.utils.SecurityUtils;
import vnikolaenko.github.network.rabbit.UserMessage;

import java.util.List;
import java.util.Optional;

/**
 * Этот контроллер нужен для создания, удаления и получения сохранений внутри проекта
 */
@RestController
@AllArgsConstructor
@RequestMapping("project-saves")
public class SavingsController {
    private final SecurityUtils securityUtils;
    private final MinioService minioService;
    private ProjectService projectService;
    private final RabbitService rabbitService;
    private final UserService userService;
    private final SavingService savingService;
    private final ProjectAccessService projectAccessService;

    /**
     * Получение всех сохранений проекта
     */
    @GetMapping("get-all/{projectId}")
    public ResponseEntity<List<Saving>> getProjectSavings(@PathVariable Long projectId) {
        projectAccessService.validateProjectAccess(projectId);
        List<Saving> savings = savingService.getSavingsByProject(projectId);
        return ResponseEntity.ok(savings);
    }

    /**
     * Получение конкретного сохранения
      */
    @GetMapping("get/{id}")
    public ResponseEntity<Saving> getSaving(@PathVariable Long id) {
        Optional<Saving> saving = savingService.getSavingById(id);
        if (saving.isEmpty()) {
            throw new AccessDeniedException("Saving not found");
        }
        projectAccessService.validateProjectAccess(saving.get().getProject().getId());
        return ResponseEntity.ok(saving.get());
    }

    /**
     * Получение конкретного сохранения
     */
    @GetMapping("has-project-saves/{id}")
    public ResponseEntity<Boolean> hasProjectSavings(@PathVariable Long id) {
        Optional<Saving> saving = savingService.getSavingById(id);
        if (saving.isEmpty()) {
            return ResponseEntity.ok(Boolean.FALSE);
        }
        projectAccessService.validateProjectAccess(saving.get().getProject().getId());
        return ResponseEntity.ok(Boolean.TRUE);
    }

    /**
     * Получение конкретного сохранения
     */
    @DeleteMapping("delete/{id}")
    public ResponseEntity<Saving> deleteSaving(@PathVariable Long id) {
        Optional<Saving> saving = savingService.getSavingById(id);
        if (saving.isEmpty()) {
            throw new AccessDeniedException("Saving not found");
        }
        projectAccessService.validateProjectAccess(saving.get().getProject().getId());
        return ResponseEntity.ok(saving.get());
    }

    /**
     * Создание сохранения внутри проекта
     */
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
        saving.setProject(project);
        saving.setName(saveName);
        savingService.save(saving);

        minioService.uploadFile(entityConfigFile.getBytes(), saveName + "_" + project.getName() + "_entity.json");
        minioService.uploadFile(appConfigFile.getBytes(), saveName + "_" + project.getName() + "_config.json");

        rabbitService.sendUserMessage(new UserMessage(username, username + " создал новое сохранение под названием " + saveName + ".",
                userService.getUserEmail(username)));
        return ResponseEntity.ok().build();
    }
}
