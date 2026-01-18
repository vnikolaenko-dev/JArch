package vnikolaenko.github.jarch.generator.generator;

import vnikolaenko.github.jarch.generator.config.ApplicationConfig;
import vnikolaenko.github.jarch.generator.template.ApplicationPropertiesTemplate;
import vnikolaenko.github.jarch.utils.LogCollector;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class ApplicationFileGenerator {
    private final ApplicationPropertiesTemplate propertiesGenerator;
    private final LogCollector logCollector;

    public void generateApplicationFiles(ApplicationConfig config, Path targetDir) throws IOException {
        // Создаем структуру директорий
        createCompleteProjectStructure(config, targetDir);

        // Генерируем главный класс приложения
        generateMainApplication(config, targetDir);

        // Генерируем файлы конфигурации
        propertiesGenerator.generateApplicationProperties(config, targetDir);

        logCollector.info("Application files generated successfully");
    }

    private void createCompleteProjectStructure(ApplicationConfig config, Path targetDir) throws IOException {
        String packagePath = config.getBasePackage().replace('.', '/');

        // Создаем ВСЕ необходимые директории рекурсивно
        Path baseDir = targetDir.toAbsolutePath();

        // Основные директории
        Path[] directories = {
                baseDir.resolve("src/main/java/" + packagePath),
                baseDir.resolve("src/main/resources"),
                baseDir.resolve("src/test/java/" + packagePath),
                baseDir.resolve("src/test/resources"),
                // Пакеты для сущностей
                baseDir.resolve("src/main/java/" + packagePath + "/model"),
                baseDir.resolve("src/main/java/" + packagePath + "/dto"),
                baseDir.resolve("src/main/java/" + packagePath + "/repository"),
                baseDir.resolve("src/main/java/" + packagePath + "/service"),
                baseDir.resolve("src/main/java/" + packagePath + "/controller")
        };

        for (Path dir : directories) {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        }
    }

    private void generateMainApplication(ApplicationConfig config, Path targetDir) throws IOException {
        String packagePath = config.getBasePackage().replace('.', '/');
        String content = """
            package %s;
            
            import org.springframework.boot.SpringApplication;
            import org.springframework.boot.autoconfigure.SpringBootApplication;
            
            @SpringBootApplication
            public class MainApplication {
                
                public static void main(String[] args) {
                    SpringApplication.run(MainApplication.class, args);
                }
            }
            """.formatted(config.getBasePackage());

        Files.writeString(Paths.get(targetDir.getFileName() + "/src/main/java", packagePath, "MainApplication.java"), content);
        logCollector.info("Generated MainApplication.java");
    }
}
