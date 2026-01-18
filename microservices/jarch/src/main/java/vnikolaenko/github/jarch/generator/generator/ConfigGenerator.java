package vnikolaenko.github.jarch.generator.generator;


import com.squareup.javapoet.*;
import vnikolaenko.github.jarch.utils.LogCollector;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class ConfigGenerator {
    private final LogCollector logCollector;

    /**
     * Генерирует класс конфигурации для ModelMapper
     */
    public static void generateModelMapperConfig(String basePackage, Path targetDir) throws IOException {
        String className = "ModelMapperConfig";

        TypeSpec configClass = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createConfigurationAnnotation())
                .addMethod(createModelMapperMethod())
                .build();

        writeJavaFile(basePackage + ".config", configClass, targetDir);
    }

    /**
     * Создает метод для создания бина ModelMapper
     */
    private static MethodSpec createModelMapperMethod() {
        return MethodSpec.methodBuilder("modelMapper")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createBeanAnnotation())
                .returns(ClassName.get("org.modelmapper", "ModelMapper"))
                .addStatement("$T modelMapper = new $T()",
                        ClassName.get("org.modelmapper", "ModelMapper"),
                        ClassName.get("org.modelmapper", "ModelMapper"))
                .addStatement("// Настройка ModelMapper при необходимости")
                .addStatement("// modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT)")
                .addStatement("return modelMapper")
                .build();
    }

    /**
     * Создает аннотацию @Configuration
     */
    private static AnnotationSpec createConfigurationAnnotation() {
        return AnnotationSpec.builder(ClassName.get("org.springframework.context.annotation", "Configuration"))
                .build();
    }

    /**
     * Создает аннотацию @Bean
     */
    private static AnnotationSpec createBeanAnnotation() {
        return AnnotationSpec.builder(ClassName.get("org.springframework.context.annotation", "Bean"))
                .build();
    }

    /**
     * Записывает сгенерированный Java файл
     */
    private static void writeJavaFile(String packageName, TypeSpec typeSpec, Path targetDir) throws IOException {
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .indent("    ")
                .build();

        javaFile.writeTo(Paths.get(targetDir + "/src/main/java"));
    }
}