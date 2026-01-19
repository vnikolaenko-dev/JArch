package vnikolaenko.github.jarch.controller;

import vnikolaenko.github.jarch.service.MinioService;
import vnikolaenko.github.jarch.utils.LogCollector;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vnikolaenko.github.jarch.generator.config.ApplicationConfig;
import vnikolaenko.github.jarch.generator.CodeGenerationOrchestrator;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vnikolaenko.github.jarch.model.FileType;
import vnikolaenko.github.jarch.service.ProjectFileService;
import vnikolaenko.github.jarch.service.SavingService;
import vnikolaenko.github.jarch.service.ProjectAccessService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/jarch/generate-project")
@AllArgsConstructor
public class GeneratorController {

    private final Map<String, GenerationData> generationStore = new ConcurrentHashMap<>();
    private final Map<String, byte[]> zipStore = new ConcurrentHashMap<>();

    private final CodeGenerationOrchestrator orchestrator;
    private final MinioService minioService;
    private final ProjectFileService projectFileService;
    private final SavingService savingService;
    private final ProjectAccessService projectAccessService;

    /**
     * Генерация проекта из сохранения
     */
    @PostMapping("/from-saving/{savingId}")
    public ResponseEntity<Map<String, String>> generateFromSaving(@PathVariable Long savingId) throws Exception {
        var saving = savingService.getSavingById(savingId)
                .orElseThrow(() -> new RuntimeException("Saving not found"));
        projectAccessService.validateProjectAccess(saving.getProject().getId());
        
        String generationId = UUID.randomUUID().toString();
        
        byte[] entityConfig = projectFileService.getFileContent(savingId, FileType.ENTITY_CONFIG);
        byte[] appConfig = projectFileService.getFileContent(savingId, FileType.APP_CONFIG);
        
        GenerationData data = new GenerationData(savingId, entityConfig, appConfig);
        generationStore.put(generationId, data);
        
        return ResponseEntity.ok(Map.of("id", generationId));
    }

    /**
     * SSE подключение для отслеживания генерации
     */
    @GetMapping("/stream/{id}")
    public SseEmitter stream(@PathVariable String id) {
        SseEmitter emitter = new SseEmitter();

        GenerationData data = generationStore.get(id);
        if (data == null) {
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

                Path entityTemp = Files.createTempFile("entity-" + id, ".json");
                Path appTemp = Files.createTempFile("app-" + id, ".json");
                
                Files.write(entityTemp, data.getEntityConfig());
                Files.write(appTemp, data.getAppConfig());

                ApplicationConfig config = ApplicationConfig.fromArgs(new String[]{
                        appTemp.toString(),
                        entityTemp.toString()
                });

                logCollector.info("📋 Конфигурация загружена");

                Path tempProjectDir = Files.createDirectory(Path.of("project-" + id));

                orchestrator.generateCompleteProject(config, tempProjectDir);

                logCollector.info("📦 Упаковка в ZIP...");

                byte[] zipBytes = createZip(tempProjectDir);
                zipStore.put(id, zipBytes);

                logCollector.getEmitter().send(SseEmitter.event().name("zipReady").data("ready"));
                logCollector.info("✅ Генерация завершена");

                Files.deleteIfExists(entityTemp);
                Files.deleteIfExists(appTemp);
                FileUtils.deleteDirectory(tempProjectDir.toFile());
                
            } catch (Exception e) {
                try {
                    logCollector.error("❌ Ошибка: " + e.getMessage());
                } catch (Exception ignored) {
                }
            } finally {
                logCollector.getEmitter().complete();
                generationStore.remove(id);
            }
        });

        return logCollector.getEmitter();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        byte[] zip = zipStore.get(id);
        zipStore.remove(id);

        if (zip == null) {
            return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=project-" + id + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }

    private byte[] createZip(Path dir) throws IOException {
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
    private static class GenerationData {
        private final Long savingId;
        private final byte[] entityConfig;
        private final byte[] appConfig;
        
        public GenerationData(Long savingId, byte[] entityConfig, byte[] appConfig) {
            this.savingId = savingId;
            this.entityConfig = entityConfig;
            this.appConfig = appConfig;
        }
        public byte[] getEntityConfig() {
            return entityConfig;
        }
        public byte[] getAppConfig() {
            return appConfig;
        }
    }
}