package github.vnikolaenko.jarch.generator.generator.included;

import com.squareup.javapoet.*;
import github.vnikolaenko.jarch.generator.auxiliary.Field;
import github.vnikolaenko.jarch.generator.auxiliary.Relation;
import github.vnikolaenko.jarch.generator.auxiliary.TypeOfRelation;
import github.vnikolaenko.jarch.generator.utils.StringUtils;
import github.vnikolaenko.jarch.generator.utils.TypeMapper;
import org.springframework.data.domain.Page;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Генератор DTO (Data Transfer Object) классов
 * Отвечает за создание классов для передачи данных между слоями приложения
 */
public class DtoGenerator {

    /**
     * Генерирует DTO класс для сущности
     *
     * @param basePackage базовый пакет приложения
     * @param entityName  имя сущности
     * @param fields      список полей сущности
     */
    public static void generateDTO(String basePackage, String entityName, List<Field> fields, Path targetDir) throws IOException {
        String className = StringUtils.capitalizeFirst(entityName) + "DTO";

        // Создаем билдер для DTO класса
        TypeSpec.Builder dtoBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createLombokDataAnnotation())
                .addAnnotation(createLombokNoArgsConstructorAnnotation())
                .addAnnotation(createLombokAllArgsConstructorAnnotation())
                .addAnnotation(createLombokBuilderAnnotation());

        // Добавляем поле ID
        dtoBuilder.addField(createIdField());

        // Добавляем все поля из entity
        for (Field field : fields) {
            FieldSpec fieldSpec = createDtoField(field, basePackage);
            if (fieldSpec != null) {
                dtoBuilder.addField(fieldSpec);
            }
        }

        TypeSpec dto = dtoBuilder.build();

        // Записываем сгенерированный файл
        writeJavaFile(basePackage + ".dto", dto, targetDir);
    }

    /**
     * Создает поле ID для DTO
     */
    private static FieldSpec createIdField() {
        return FieldSpec.builder(Long.class, "id", Modifier.PRIVATE)
                .build();
    }

    /**
     * Создает поле DTO на основе поля entity
     * Обрабатывает как обычные поля, так и отношения между сущностями
     */
    private static FieldSpec createDtoField(Field field, String basePackage) {
        String fieldName = field.getFieldName();
        String fieldType = field.getFieldType();
        Relation relation = field.getRelation();

        // Обрабатываем отношения между сущностями
        if (relation != null) {
            return createRelationField(relation, fieldName);
        }

        // Обычное поле - используем соответствующий Java тип
        return createBasicField(fieldType, fieldName, basePackage);
    }

    /**
     * Создает поле для отношений между сущностями
     * Для коллекций использует списки ID, для одиночных отношений - одиночный ID
     */
    private static FieldSpec createRelationField(Relation relation, String fieldName) {
        TypeOfRelation relationType = relation.getTypeOfRelation();

        switch (relationType) {
            case MANY_TO_MANY:
            case ONE_TO_MANY:
                // Для коллекционных отношений используем список ID
                return FieldSpec.builder(
                        ParameterizedTypeName.get(
                                ClassName.get("java.util", "List"),
                                ClassName.get(Long.class)
                        ),
                        fieldName + "Ids",
                        Modifier.PRIVATE
                ).build();

            case ONE_TO_ONE:
            case MANY_TO_ONE:
                // Для одиночных отношений используем одиночный ID
                return FieldSpec.builder(Long.class, fieldName + "Id", Modifier.PRIVATE)
                        .build();

            default:
                return FieldSpec.builder(Long.class, fieldName + "Id", Modifier.PRIVATE)
                        .build();
        }
    }

    /**
     * Создает поле для базового типа данных
     */
    private static FieldSpec createBasicField(String fieldType, String fieldName, String basePackage) {
        TypeName javaType = TypeMapper.getJavaType(fieldType, basePackage);
        return FieldSpec.builder(javaType, fieldName, Modifier.PRIVATE)
                .build();
    }

    /**
     * Создает аннотацию Lombok @Data
     */
    private static AnnotationSpec createLombokDataAnnotation() {
        return AnnotationSpec.builder(ClassName.get("lombok", "Data"))
                .build();
    }

    /**
     * Создает аннотацию Lombok @NoArgsConstructor
     */
    private static AnnotationSpec createLombokNoArgsConstructorAnnotation() {
        return AnnotationSpec.builder(ClassName.get("lombok", "NoArgsConstructor"))
                .build();
    }

    /**
     * Создает аннотацию Lombok @AllArgsConstructor
     */
    private static AnnotationSpec createLombokAllArgsConstructorAnnotation() {
        return AnnotationSpec.builder(ClassName.get("lombok", "AllArgsConstructor"))
                .build();
    }

    /**
     * Создает аннотацию Lombok @Builder
     */
    private static AnnotationSpec createLombokBuilderAnnotation() {
        return AnnotationSpec.builder(ClassName.get("lombok", "Builder"))
                .build();
    }

    /**
     * Записывает сгенерированный Java файл
     */
    private static void writeJavaFile(String packageName, TypeSpec typeSpec, Path targetDir) throws IOException {
        JavaFile.builder(packageName, typeSpec)
                .indent("    ")
                .build()
                .writeTo(Paths.get(targetDir + "/src/main/java"));
    }
}