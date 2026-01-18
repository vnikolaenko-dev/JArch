package vnikolaenko.github.jarch.controller;

import org.springframework.web.bind.annotation.*;
import vnikolaenko.github.jarch.service.MinioService;
import vnikolaenko.github.jarch.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/**
 * Контроллер для скачивания конфигов
 */
@RestController
@RequestMapping("jarch")
@AllArgsConstructor
public class DownloaderController {
    private final MinioService minioService;


    @GetMapping("/download-config-file/{project-name}/{save_name}/")
    public ResponseEntity<String> downloadConfig(@PathVariable("project-name") String projectName, @PathVariable String save_name) throws Exception {
        byte[] fileBytes = minioService.downloadFile(save_name + "_" + projectName + "_config.json");
        if (fileBytes == null) {
            return ResponseEntity.badRequest().body(null);
        }

        String jsonContent = new String(fileBytes, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonContent);
    }

    @GetMapping("/download-entity-file/{project-name}/{save_name}")
    public ResponseEntity<String> downloadEntity(@PathVariable("project-name") String projectName, @PathVariable String save_name) throws Exception {
        byte[] fileBytes = minioService.downloadFile(save_name + "_" + projectName + "_entity.json");
        if (fileBytes == null) {
            return ResponseEntity.badRequest().body(null);
        }

        String jsonContent = new String(fileBytes, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonContent);
    }
}
