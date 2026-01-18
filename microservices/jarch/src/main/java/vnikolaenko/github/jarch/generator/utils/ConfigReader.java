package vnikolaenko.github.jarch.generator.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import vnikolaenko.github.jarch.generator.config.ApplicationConfig;
import vnikolaenko.github.jarch.generator.config.EntityConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class ConfigReader {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApplicationConfig readApplicationConfig(String configPath) throws IOException {
        String content = Files.readString(Paths.get(configPath));
        return objectMapper.readValue(content, ApplicationConfig.class);
    }

    public EntityConfig readEntityConfig(String configPath) throws IOException {
        String content = Files.readString(Paths.get(configPath));
        return objectMapper.readValue(content, EntityConfig.class);
    }

    // Для обратной совместимости
    public EntityConfig readConfig(String configFile) throws IOException {
        return readEntityConfig(configFile);
    }
}
