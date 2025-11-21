package github.vnikolaenko.jarch.generator.template;


import github.vnikolaenko.jarch.generator.config.ApplicationConfig;
import github.vnikolaenko.jarch.generator.config.DatabaseConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ApplicationPropertiesTemplate {

    public void generateApplicationProperties(ApplicationConfig config, Path targetDir) throws IOException {
        String content;

        if (config.getPropertiesFormat() == ApplicationConfig.PropertiesFormat.YAML) {
            content = generateApplicationYaml(config);
        } else {
            content = generate(config);
        }

        String fileName = config.getPropertiesFormat() == ApplicationConfig.PropertiesFormat.YAML
                ? "application.yml"
                : "application.properties";

        Files.writeString(Paths.get(targetDir + "/src/main/resources", fileName), content);
    }

    private String generate(ApplicationConfig config) {
        DatabaseConfig dbConfig = config.getDatabaseConfig();

        if (dbConfig.getType() == ApplicationConfig.DatabaseType.POSTGRESQL) {
            return """
                # Server Configuration
                server.port=%d
                spring.application.name=%s
                
                # PostgreSQL Configuration
                spring.datasource.url=jdbc:postgresql://%s:%d/%s
                spring.datasource.username=%s
                spring.datasource.password=%s
                spring.datasource.driver-class-name=org.postgresql.Driver
                
                # JPA Configuration
                spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
                spring.jpa.hibernate.ddl-auto=%s
                spring.jpa.show-sql=true
                spring.jpa.properties.hibernate.format_sql=true
                
                # Connection Pool
                spring.datasource.hikari.maximum-pool-size=%d
                spring.datasource.hikari.connection-timeout=20000
                spring.datasource.hikari.idle-timeout=300000
                
                # Logging
                logging.level.%s=DEBUG
                """.formatted(
                    config.getServerPort(),
                    config.getApplicationName(),
                    dbConfig.getHost(),
                    dbConfig.getPort(),
                    dbConfig.getDatabaseName(),
                    dbConfig.getUsername(),
                    dbConfig.getPassword(),
                    dbConfig.getDdlAuto(),
                    dbConfig.getPoolSize(),
                    config.getBasePackage()
            );
        } else {
            // H2 Configuration
            return """
                # Server Configuration
                server.port=%d
                spring.application.name=%s
                
                # H2 Database
                spring.datasource.url=jdbc:h2:mem:testdb
                spring.datasource.driver-class-name=org.h2.Driver
                spring.datasource.username=sa
                spring.datasource.password=
                
                # JPA Configuration
                spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
                spring.jpa.hibernate.ddl-auto=create-drop
                spring.jpa.show-sql=true
                spring.jpa.properties.hibernate.format_sql=true
                
                # H2 Console
                spring.h2.console.enabled=true
                spring.h2.console.path=/h2-console
                
                # Logging
                logging.level.%s=DEBUG
                """.formatted(
                    config.getServerPort(),
                    config.getApplicationName(),
                    config.getBasePackage()
            );
        }
    }

    private String generateApplicationYaml(ApplicationConfig config) {
        DatabaseConfig dbConfig = config.getDatabaseConfig();

        if (dbConfig.getType() == ApplicationConfig.DatabaseType.POSTGRESQL) {
            return """
                server:
                  port: %d
                  
                spring:
                  application:
                    name: %s
                    
                  datasource:
                    url: jdbc:postgresql://%s:%d/%s
                    username: %s
                    password: %s
                    driver-class-name: org.postgresql.Driver
                    hikari:
                      maximum-pool-size: %d
                      connection-timeout: 20000
                      idle-timeout: 300000
                      
                  jpa:
                    database-platform: org.hibernate.dialect.PostgreSQLDialect
                    hibernate:
                      ddl-auto: %s
                    show-sql: true
                    properties:
                      hibernate:
                        format_sql: true
                        
                logging:
                  level:
                    %s: DEBUG
                """.formatted(
                    config.getServerPort(),
                    config.getApplicationName(),
                    dbConfig.getHost(),
                    dbConfig.getPort(),
                    dbConfig.getDatabaseName(),
                    dbConfig.getUsername(),
                    dbConfig.getPassword(),
                    dbConfig.getPoolSize(),
                    dbConfig.getDdlAuto(),
                    config.getBasePackage()
            );
        } else {
            return """
                server:
                  port: %d
                  
                spring:
                  application:
                    name: %s
                    
                  datasource:
                    url: jdbc:h2:mem:testdb
                    driver-class-name: org.h2.Driver
                    username: sa
                    password: ''
                    
                  jpa:
                    database-platform: org.hibernate.dialect.H2Dialect
                    hibernate:
                      ddl-auto: create-drop
                    show-sql: true
                    properties:
                      hibernate:
                        format_sql: true
                        
                  h2:
                    console:
                      enabled: true
                      path: /h2-console
                      
                logging:
                  level:
                    %s: DEBUG
                """.formatted(
                    config.getServerPort(),
                    config.getApplicationName(),
                    config.getBasePackage()
            );
        }
    }
}
