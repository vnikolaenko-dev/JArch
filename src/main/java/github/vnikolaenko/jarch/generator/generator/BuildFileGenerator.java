package github.vnikolaenko.jarch.generator.generator;

import github.vnikolaenko.jarch.generator.config.ApplicationConfig;
import github.vnikolaenko.jarch.utils.LogCollector;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class BuildFileGenerator {
    private final LogCollector logCollector;

    public void generateBuildFiles(ApplicationConfig config, Path targetDir) throws IOException {
        // Создаем основную директорию generated
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
            logCollector.info("📁 Created directory: " + targetDir.toAbsolutePath());
        }

        if (config.getBuildTool() == ApplicationConfig.BuildTool.MAVEN) {
            generateMavenPom(config);
        } else {
            generateGradleBuild(config);
        }
    }

    private void generateMavenPom(ApplicationConfig config) throws IOException {
        String artifactId = extractArtifactId(config.getBasePackage());
        String databaseDependency = getDatabaseDependency(config);

        String content = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                     http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                
                <groupId>%s</groupId>
                <artifactId>%s</artifactId>
                <version>1.0.0</version>
                <packaging>jar</packaging>
                
                <parent>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-parent</artifactId>
                    <version>3.2.0</version>
                    <relativePath/>
                </parent>
                
                <properties>
                    <java.version>17</java.version>
                    <maven.compiler.source>17</maven.compiler.source>
                    <maven.compiler.target>17</maven.compiler.target>
                </properties>
                
                <dependencies>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-web</artifactId>
                    </dependency>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-data-jpa</artifactId>
                    </dependency>
                     <dependency>
                        <groupId>org.modelmapper</groupId>
                        <artifactId>modelmapper</artifactId>
                        <version>3.1.1</version>
                     </dependency>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-validation</artifactId>
                    </dependency>
                    %s
                    <dependency>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <optional>true</optional>
                    </dependency>
                </dependencies>
                
                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-maven-plugin</artifactId>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """.formatted(config.getBasePackage(), artifactId, databaseDependency);

        Files.writeString(Paths.get("/pom.xml"), content);
        logCollector.info("Generated pom.xml");
    }

    private void generateGradleBuild(ApplicationConfig config) throws IOException {
        String databaseDependency = getGradleDatabaseDependency(config);

        String content = """
            plugins {
                id 'org.springframework.boot' version '3.2.0'
                id 'io.spring.dependency-management' version '1.1.4'
                id 'java'
            }
            
            group = '%s'
            version = '1.0.0'
            sourceCompatibility = '17'
            
            configurations {
                compileOnly {
                    extendsFrom annotationProcessor
                }
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation 'org.springframework.boot:spring-boot-starter-web'
                implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
                implementation 'org.springframework.boot:spring-boot-starter-validation'
                %s
                compileOnly 'org.projectlombok:lombok'
                annotationProcessor 'org.projectlombok:lombok'
            }
            
            tasks.named('test') {
                useJUnitPlatform()
            }
            """.formatted(config.getBasePackage(), databaseDependency);

        Files.writeString(Paths.get("./generated/build.gradle"), content);
        logCollector.info("Generated build.gradle");
    }

    private String extractArtifactId(String basePackage) {
        return basePackage.substring(basePackage.lastIndexOf('.') + 1);
    }

    private String getDatabaseDependency(ApplicationConfig config) {
        return switch (config.getDatabaseConfig().getType()) {
            case POSTGRESQL -> """
                    <dependency>
                        <groupId>org.postgresql</groupId>
                        <artifactId>postgresql</artifactId>
                        <scope>runtime</scope>
                    </dependency>
                    """;
            case MYSQL -> """
                    <dependency>
                        <groupId>com.mysql</groupId>
                        <artifactId>mysql-connector-j</artifactId>
                        <scope>runtime</scope>
                    </dependency>
                    """;
            default -> """
                    <dependency>
                        <groupId>com.h2database</groupId>
                        <artifactId>h2</artifactId>
                        <scope>runtime</scope>
                    </dependency>
                    """;
        };
    }

    private String getGradleDatabaseDependency(ApplicationConfig config) {
        return switch (config.getDatabaseConfig().getType()) {
            case POSTGRESQL -> "runtimeOnly 'org.postgresql:postgresql'";
            case MYSQL -> "runtimeOnly 'com.mysql:mysql-connector-j'";
            default -> "runtimeOnly 'com.h2database:h2'";
        };
    }
}