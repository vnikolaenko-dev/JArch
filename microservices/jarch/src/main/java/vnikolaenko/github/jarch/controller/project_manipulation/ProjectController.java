package vnikolaenko.github.jarch.controller.project_manipulation;

import vnikolaenko.github.jarch.model.Project;
import vnikolaenko.github.jarch.service.ProjectService;
import vnikolaenko.github.jarch.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Управление проектами
 */
@RestController
@AllArgsConstructor
@RequestMapping("/project")
public class ProjectController {
    private ProjectService projectService;
    private SecurityUtils securityUtils;

    /**
     * @return Список проектов, которые пользователь создал
     */
    @GetMapping("/all")
    public ResponseEntity<List<Project>> getAllUserProject() {
        String username = securityUtils.getCurrentUsername();
        return ResponseEntity.ok().body(projectService.getAllUserProjects(username));
    }

    /**
     * @return Список проектов, в которые пользователь был добавлен
     */
    @GetMapping("/joined")
    public ResponseEntity<List<Project>> getAllJoinedProject() {
        String username = securityUtils.getCurrentUsername();
        return ResponseEntity.ok().body(projectService.getAllUserJoinedProjects(username));
    }

    /**
     * @return Проект по названию
     */
    @GetMapping
    public ResponseEntity<Project> getProject(@RequestParam String projectName) {
        String username = securityUtils.getCurrentUsername();
        return ResponseEntity.ok().body(projectService.getUserProject(username, projectName));
    }



    /**
     * Создание проекта
     * @param project описание проекта для отображения карточки проекта
     * @return -
     */
    @PostMapping("/save")
    public ResponseEntity<Void> saveProject(@RequestBody Project project) {
        projectService.save(project);
        return ResponseEntity.ok().build();
    }

    /**
     * Обновление проекта
     * @param project описание проекта для отображения карточки проекта
     * @return -
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> saveProject(@PathVariable Long id, @RequestBody Project project) {
        projectService.update(id, project);
        return ResponseEntity.ok().build();
    }
}
