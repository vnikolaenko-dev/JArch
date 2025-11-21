package github.vnikolaenko.jarch.generator.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import github.vnikolaenko.jarch.generator.utils.ConfigReader;


@Data
@NoArgsConstructor
public class ApplicationConfig {
    public enum BuildTool {
        MAVEN, GRADLE
    }

    public enum PropertiesFormat {
        PROPERTIES, YAML
    }

    public enum DatabaseType {
        H2, POSTGRESQL, MYSQL
    }

    @JsonProperty("basePackage")
    private String basePackage = "com.myapp";

    @JsonProperty("applicationName")
    private String applicationName = "My Application";

    @JsonProperty("buildTool")
    private BuildTool buildTool = BuildTool.MAVEN;

    @JsonProperty("propertiesFormat")
    private PropertiesFormat propertiesFormat = PropertiesFormat.PROPERTIES;

    @JsonProperty("serverPort")
    private int serverPort = 8080;

    @JsonProperty("database")
    private DatabaseConfig databaseConfig = new DatabaseConfig();

    private String appConfigPath;  // Путь к app-config.json
    private String entityConfigPath; // Путь к entity-config.json

    public static ApplicationConfig fromArgs(String[] args) {
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }

        ApplicationConfig config = new ApplicationConfig();
        config.setAppConfigPath(args[0]);
        config.setEntityConfigPath(args[1]);

        // Загружаем настройки из app-config.json
        try {
            ConfigReader configReader = new ConfigReader();
            ApplicationConfig loadedConfig = configReader.readApplicationConfig(args[0]);

            // Копируем все поля из загруженного конфига
            config.setBasePackage(loadedConfig.getBasePackage());
            config.setApplicationName(loadedConfig.getApplicationName());
            config.setBuildTool(loadedConfig.getBuildTool());
            config.setPropertiesFormat(loadedConfig.getPropertiesFormat());
            config.setServerPort(loadedConfig.getServerPort());
            config.setDatabaseConfig(loadedConfig.getDatabaseConfig());

        } catch (Exception e) {
            System.err.println("❌ Error loading app config from: " + args[0]);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

        return config;
    }

    private static void printUsage() {
        System.out.println("🚀 Usage: java CodeGeneratorApplication <app-config.json> <entity-config.json>");
        System.out.println("📋 Example: java CodeGeneratorApplication app-config.json entity-config.json");
        System.out.println("");
        System.out.println("📁 app-config.json - application settings (database, build tool, etc.)");
        System.out.println("📁 entity-config.json - entity definitions");
    }
}