package vnikolaenko.github.jarch.controller.project_manipulation;

import org.springframework.http.HttpStatus;
import vnikolaenko.github.jarch.model.TeamMember;
import vnikolaenko.github.jarch.service.ProjectService;
import vnikolaenko.github.jarch.service.TeamMemberService;
import vnikolaenko.github.jarch.service.UserService;
import vnikolaenko.github.jarch.utils.SecurityUtils;
import vnikolaenko.github.jarch.service.RabbitService;
import vnikolaenko.github.network.rabbit.UserMessage;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления командой внутри проекта
 */
@RestController
@AllArgsConstructor
@RequestMapping("team")
public class TeamController {
    private final TeamMemberService teamMemberService;
    private final UserService userService;
    private final SecurityUtils securityUtils;
    private final RabbitService rabbitService;
    private final ProjectService projectService;

    /**
     * @return получить список всех людкй внутри команды
     */
    @GetMapping
    public ResponseEntity<List<TeamMember>> getTeamMembers(@RequestParam long projectId) {
        return ResponseEntity.ok().body(teamMemberService.findAllByProjectId(projectId));
    }

    /**
     * Добавить человека в команду
     */
    @PostMapping
    public ResponseEntity<String> createTeamMember(@RequestBody TeamMember teamMember, @RequestParam long projectId) {
        if (!userService.doesUserExist(teamMember.getUsername())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Такого пользователя не существует.");
        }
        String username = securityUtils.getCurrentUsername();
        teamMemberService.save(teamMember, projectId, username);
        rabbitService.sendUserMessage(new UserMessage(username, username + " добавил тебя в проект " + projectService.getProjectById(projectId).getName() + ".",
                userService.getUserEmail(teamMember.getUsername())));
        return ResponseEntity.ok().build();
    }

    /**
     * Удалить человека из команды
     */
    @DeleteMapping
    public ResponseEntity<String> removeTeamMember(@RequestParam String teamMember, @RequestParam long projectId) {
        if (!userService.doesUserExist(teamMember)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Такого пользователя не существует.");
        }
        String username = securityUtils.getCurrentUsername();
        teamMemberService.delete(teamMember, projectId, username);
        rabbitService.sendUserMessage(new UserMessage(username, username + " удалил тебя из проекта " + projectService.getProjectById(projectId).getName() + ".",
                userService.getUserEmail(teamMember)));
        return ResponseEntity.ok().build();
    }
}
