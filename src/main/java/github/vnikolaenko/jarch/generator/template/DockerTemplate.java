package github.vnikolaenko.jarch.generator.template;

import github.vnikolaenko.jarch.generator.config.ApplicationConfig;
import org.springframework.stereotype.Service;

@Service
public class DockerTemplate {

    public static String generateDockerfile(ApplicationConfig config) {
        String buildCommand = config.getBuildTool() == ApplicationConfig.BuildTool.MAVEN ?
                "./mvnw clean package" : "./gradlew clean build";
        String jarPath = config.getBuildTool() == ApplicationConfig.BuildTool.MAVEN ?
                "target/*.jar" : "build/libs/*.jar";

        return """
            # Build stage
            FROM maven:3.8.4-openjdk-17 AS build
            COPY . .
            RUN %s -DskipTests
            
            # Runtime stage
            FROM openjdk:17-jdk-slim
            COPY --from=build %s app.jar
            EXPOSE 8080
            ENTRYPOINT ["java", "-jar", "/app.jar"]
            """.formatted(buildCommand, jarPath);
    }
}
