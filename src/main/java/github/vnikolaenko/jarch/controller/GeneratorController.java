package github.vnikolaenko.jarch.controller;

import github.vnikolaenko.jarch.utils.LogCollector;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import github.vnikolaenko.jarch.generator.config.ApplicationConfig;
import github.vnikolaenko.jarch.generator.CodeGenerationOrchestrator;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/generate-project")
@AllArgsConstructor

public class GeneratorController {

    private final Map<String, TempFiles> fileStore = new ConcurrentHashMap<>();
    private final Map<String, byte[]> zipStore = new ConcurrentHashMap<>();

    private final CodeGenerationOrchestrator orchestrator;


    /**
     * 1) Загружаем файлы — POST
     * Возвращаем уникальный ID
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFiles(
            @RequestParam("entityConfig") MultipartFile entityConfigFile,
            @RequestParam("appConfig") MultipartFile appConfigFile) throws IOException {

        if (entityConfigFile.isEmpty() || appConfigFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Оба файла обязательны"));
        }

        // Генерируем ID запроса
        String id = UUID.randomUUID().toString();

        // Создаём временные файлы
        Path entityTemp = Files.createTempFile("entity-" + id, ".json");
        Path appTemp = Files.createTempFile("app-" + id, ".json");

        entityConfigFile.transferTo(entityTemp);
        appConfigFile.transferTo(appTemp);

        fileStore.put(id, new TempFiles(entityTemp, appTemp));

        return ResponseEntity.ok(Map.of("id", id));
    }


    /**
     * 2) SSE подключение — GET /stream/{id}
     */
    @GetMapping("/stream/{id}")
    public SseEmitter stream(@PathVariable String id) {
        SseEmitter emitter = new SseEmitter();

        TempFiles files = fileStore.get(id);
        if (files == null) {
            try {
                emitter.send(SseEmitter.event().name("error").data("Файлы не найдены"));
            } catch (IOException ignored) {
            }
            emitter.complete();
            return emitter;
        }

        LogCollector logCollector = new LogCollector(emitter);

        CompletableFuture.runAsync(() -> {
            try {
                logCollector.info("🚀 Начало генерации проекта...");

                // Загружаем конфигурацию
                ApplicationConfig config = ApplicationConfig.fromArgs(new String[]{
                        files.appConfig().toString(),
                        files.entityConfig().toString()
                });

                logCollector.info("📋 Конфигурация загружена");

                // Временная директория для генерации
                Path tempProjectDir = Files.createDirectory(Path.of("project-" + id));

                orchestrator.generateCompleteProject(config, tempProjectDir);

                logCollector.info("📦 Упаковка в ZIP...");

                // Создаём ZIP
                byte[] zipBytes = createZip(tempProjectDir);
                zipStore.put(id, zipBytes);

                logCollector.getEmitter().send(SseEmitter.event().name("zipReady").data("ready"));
                logCollector.info("✅ Генерация завершена");
                System.out.println("Очистка " + tempProjectDir.toAbsolutePath());
                FileUtils.deleteDirectory(tempProjectDir.toFile());
            } catch (Exception e) {
                try {
                    logCollector.error("❌ Ошибка: " + e.getMessage());
                } catch (Exception ignored) {
                }
            } finally {
                logCollector.getEmitter().complete();
            }
        });

        return logCollector.getEmitter();
    }


    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable String id) {

        byte[] zip = zipStore.get(id);

        if (zip == null) {
            return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=project-" + id + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }




    public byte[] createZip(Path dir) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            zos.putNextEntry(new ZipEntry(dir.relativize(path).toString()));
                            zos.write(Files.readAllBytes(path));
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        return baos.toByteArray();
    }

    private record TempFiles(Path entityConfig, Path appConfig) {
    }
}