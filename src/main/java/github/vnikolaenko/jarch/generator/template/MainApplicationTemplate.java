package github.vnikolaenko.jarch.generator.template;

import github.vnikolaenko.jarch.generator.config.ApplicationConfig;
import org.springframework.stereotype.Service;

@Service
public class MainApplicationTemplate {

    public static String generateMainApplication(ApplicationConfig config) {
        String packageName = config.getBasePackage();

        return """
            package %s;
            
            import org.springframework.boot.SpringApplication;
            import org.springframework.boot.autoconfigure.SpringBootApplication;
            
            @SpringBootApplication
            public class MainApplication {
                
                public static void main(String[] args) {
                    SpringApplication.run(MainApplication.class, args);
                }
            }
            """.formatted(packageName);
    }
}