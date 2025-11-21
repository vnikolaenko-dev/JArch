package github.vnikolaenko.jarch.generator;

import github.vnikolaenko.jarch.generator.template.BuildTemplate;
import github.vnikolaenko.jarch.generator.template.DockerTemplate;
import github.vnikolaenko.jarch.utils.LogCollector;
import lombok.AllArgsConstructor;
import github.vnikolaenko.jarch.generator.config.ApplicationConfig;
import github.vnikolaenko.jarch.generator.config.EntityConfig;
import github.vnikolaenko.jarch.generator.generator.ApplicationFileGenerator;
import github.vnikolaenko.jarch.generator.generator.BuildFileGenerator;
import github.vnikolaenko.jarch.generator.generator.EntityGenerator;
import github.vnikolaenko.jarch.generator.utils.ConfigReader;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class CodeGenerationOrchestrator {

    private final ConfigReader configReader;
    private final EntityGenerator entityGenerator;
    private final ApplicationFileGenerator applicationFileGenerator;
    private final LogCollector logCollector;



    // Новый метод с поддержкой целевой директории
    public void generateCompleteProject(ApplicationConfig appConfig, Path targetDir) throws Exception {
        logCollector.info("📖 Reading entity configuration from: " + appConfig.getEntityConfigPath());

        // Создаем целевую директорию если её нет
        if (!targetDir.toFile().exists()) {
            targetDir.toFile().mkdirs();
        }

        // Чтение конфигурации сущностей из указанного файла
        EntityConfig entityConfig = configReader.readEntityConfig(appConfig.getEntityConfigPath());

        // Генерация структуры проекта
        generateProjectStructure(appConfig, entityConfig, targetDir);

        logCollector.info("✅ Generated project with " + entityConfig.getEntities().size() + " entities");
        logCollector.info("📦 Build tool: " + appConfig.getBuildTool());
        logCollector.info("🏠 Base package: " + appConfig.getBasePackage());
        logCollector.info("🗄️ Database: " + appConfig.getDatabaseConfig().getType());
        logCollector.info("🔧 Properties format: " + appConfig.getPropertiesFormat());

        printSetupInstructions(appConfig);
    }

    private void generateProjectStructure(ApplicationConfig appConfig, EntityConfig entityConfig, Path targetDir) throws Exception {
        String config = BuildTemplate.generateBuildFile(appConfig);
        if (appConfig.getBuildTool().equals(ApplicationConfig.BuildTool.MAVEN)) {
            Files.writeString(Paths.get(targetDir + "/pom.xml"), config);
        } else {
            Files.writeString(Paths.get(targetDir + "/build.gradle"), config);
        }

        String docker = DockerTemplate.generateDockerfile(appConfig);
        Files.writeString(Paths.get(targetDir + "/Dockerfile"), docker);

        // Генерация основных классов приложения
        applicationFileGenerator.generateApplicationFiles(appConfig, targetDir);

        // Генерация сущностей и связанных компонентов
        entityGenerator.generateAllEntities(appConfig, entityConfig, targetDir);
    }

    private void printSetupInstructions(ApplicationConfig config) {
        logCollector.info("\n🚀 SETUP INSTRUCTIONS:");

        if (config.getDatabaseConfig().getType() == ApplicationConfig.DatabaseType.POSTGRESQL) {
            logCollector.info("1. Start PostgreSQL: " +
                    config.getDatabaseConfig().getHost() + ":" + config.getDatabaseConfig().getPort());
            logCollector.info("2. Create database: " + config.getDatabaseConfig().getDatabaseName());
            logCollector.info("3. Update credentials in application." +
                    (config.getPropertiesFormat() == ApplicationConfig.PropertiesFormat.YAML ? "yml" : "properties"));
        } else {
            logCollector.info("1. H2 database will start automatically");
            logCollector.info("2. H2 console: http://localhost:" + config.getServerPort() + "/h2-console");
        }

        if (config.getBuildTool() == ApplicationConfig.BuildTool.MAVEN) {
            logCollector.info("3. Run: mvn spring-boot:run");
        } else {
            logCollector.info("3. Run: ./gradlew bootRun");
        }
    }
}