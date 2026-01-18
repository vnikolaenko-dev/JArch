package vnikolaenko.github.jarch.controller.project_manipulation;

import vnikolaenko.github.jarch.model.TeamMember;
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
    public ResponseEntity<Void> createTeamMember(@RequestBody TeamMember teamMember, @RequestParam long projectId) {
        String username = securityUtils.getCurrentUsername();
        teamMemberService.save(teamMember, projectId, username);
        rabbitService.sendUserMessage(new UserMessage(username, "Добавил тебя в проект!",
                userService.getUserEmail(teamMember.getUsername())));
        return ResponseEntity.ok().build();
    }

    /**
     * Удалить человека из команды
     */
    @DeleteMapping
    public ResponseEntity<Void> removeTeamMember(@RequestParam String teamMember, @RequestParam long projectId) {
        String username = securityUtils.getCurrentUsername();
        teamMemberService.delete(teamMember, projectId, username);
        rabbitService.sendUserMessage(new UserMessage(username, "Удалил тебя из проекта!",
                userService.getUserEmail(teamMember)));
        return ResponseEntity.ok().build();
    }
}
