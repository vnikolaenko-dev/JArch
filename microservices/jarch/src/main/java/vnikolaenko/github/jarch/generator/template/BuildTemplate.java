package vnikolaenko.github.jarch.generator.template;

import vnikolaenko.github.jarch.generator.config.ApplicationConfig;
import org.springframework.stereotype.Service;

@Service
public class BuildTemplate {

    public static String generateBuildFile(ApplicationConfig config) {
        if (config.getBuildTool() == ApplicationConfig.BuildTool.MAVEN) {
            return generateMavenPom(config);
        } else {
            return generateGradleBuild(config);
        }
    }

    private static String generateMavenPom(ApplicationConfig config) {
        String artifactId = config.getBasePackage().substring(config.getBasePackage().lastIndexOf('.') + 1);

        return """
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
                            <version>3.5.4</version>
                            <relativePath/>
                        </parent>
                
                        <properties>
                            <java.version>21</java.version>
                            <maven.compiler.source>21</maven.compiler.source>
                            <maven.compiler.target>21</maven.compiler.target>
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
                        <groupId>com.h2database</groupId>
                        <artifactId>h2</artifactId>
                        <scope>runtime</scope>
                    </dependency>
                    <dependency>
                         <groupId>org.projectlombok</groupId>
                         <artifactId>lombok</artifactId>
                         <version>1.18.34</version>
                         <scope>provided</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.modelmapper</groupId>
                        <artifactId>modelmapper</artifactId>
                        <version>3.1.1</version>
                    </dependency>
                            <dependency>
                                    <groupId>org.springdoc</groupId>
                                    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                                    <version>2.8.4</version>
                                </dependency>
                </dependencies>
                
                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-maven-plugin</artifactId>
                            <configuration>
                                <excludes>
                                    <exclude>
                                        <groupId>org.projectlombok</groupId>
                                        <artifactId>lombok</artifactId>
                                    </exclude>
                                </excludes>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """.formatted(config.getBasePackage(), artifactId);
    }

    private static String generateGradleBuild(ApplicationConfig config) {
        return """
            plugins {
                id 'org.springframework.boot' version '3.5.4'
                id 'io.spring.dependency-management' version '1.1.0'
                id 'java'
            }
            
            group = '%s'
            version = '1.0.0'
            sourceCompatibility = '21'
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation 'org.springframework.boot:spring-boot-starter-web'
                implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
                implementation 'org.springframework.boot:spring-boot-starter-validation'
                implementation 'org.projectlombok:lombok'
                implementation 'org.modelmapper:modelmapper:3.1.1'
                implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4'
                runtimeOnly 'com.h2database:h2'
                annotationProcessor 'org.projectlombok:lombok'
            }
            
            tasks.named('test') {
                useJUnitPlatform()
            }
            """.formatted(config.getBasePackage());
    }
}
