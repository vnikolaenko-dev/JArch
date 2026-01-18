package vnikolaenko.github.jarch.generator.generator.included;

import com.squareup.javapoet.*;
import vnikolaenko.github.jarch.generator.auxiliary.Field;
import vnikolaenko.github.jarch.generator.auxiliary.Relation;
import vnikolaenko.github.jarch.generator.auxiliary.TypeOfRelation;
import vnikolaenko.github.jarch.generator.utils.StringUtils;
import vnikolaenko.github.jarch.generator.utils.TypeMapper;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Генератор JPA Entity классов
 * Отвечает за создание классов сущностей для работы с базой данных
 */
public class ModelGenerator {

    /**
     * Генерирует JPA Entity класс
     *
     * @param basePackage базовый пакет приложения
     * @param entityName имя сущности
     * @param fields список полей сущности
     */
    public static void generateEntity(String basePackage, String entityName, List<Field> fields, Path targetDir) throws IOException {
        String className = StringUtils.capitalizeFirst(entityName);
        System.out.println("Генерация класса сущности: " + className);

        // Создаем билдер для класса сущности
        TypeSpec.Builder modelBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createEntityAnnotation())
                .addAnnotation(createLombokDataAnnotation())
                .addAnnotation(createLombokNoArgsConstructorAnnotation())
                .addAnnotation(createLombokAllArgsConstructorAnnotation())
                .addField(createIdField());

        // Обрабатываем все поля сущности
        for (Field field : fields) {
            FieldSpec fieldSpec = createFieldSpec(field, basePackage);
            if (fieldSpec != null) {
                modelBuilder.addField(fieldSpec);
                System.out.println("Добавлено поле: " + field.getFieldName() +
                        " с отношением: " + (field.getRelation() != null ?
                        field.getRelation().getTypeOfRelation() : "отсутствует"));
            }
        }

        TypeSpec model = modelBuilder.build();

        // Записываем сгенерированный файл
        writeJavaFile(basePackage + ".model", model, targetDir);
        System.out.println("Успешно сгенерирован: " + className + ".java");
    }

    /**
     * Создает поле ID с JPA аннотациями
     */
    private static FieldSpec createIdField() {
        return FieldSpec.builder(Long.class, "id", Modifier.PRIVATE)
                .addAnnotation(createIdAnnotation())
                .addAnnotation(createGeneratedValueAnnotation())
                .build();
    }

    /**
     * Создает спецификацию поля с поддержкой JPA отношений
     */
    private static FieldSpec createFieldSpec(Field field, String basePackage) {
        String fieldName = field.getFieldName();
        String fieldType = field.getFieldType();
        Relation relation = field.getRelation();

        TypeName fieldTypeName = TypeMapper.getFieldType(fieldType, basePackage);
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldTypeName, fieldName, Modifier.PRIVATE);

        // Добавляем JPA аннотации в зависимости от типа поля
        if (relation != null) {
            addRelationAnnotations(fieldBuilder, relation, fieldName);
        } else if (TypeMapper.isBasicType(fieldType)) {
            addColumnAnnotation(fieldBuilder, fieldName);
        }

        return fieldBuilder.build();
    }

    /**
     * Добавляет аннотации для отношений между сущностями
     */
    private static void addRelationAnnotations(FieldSpec.Builder fieldBuilder, Relation relation, String fieldName) {
        TypeOfRelation relationType = relation.getTypeOfRelation();

        switch (relationType) {
            case ONE_TO_ONE:
                fieldBuilder.addAnnotation(createOneToOneAnnotation());
                fieldBuilder.addAnnotation(createJoinColumnAnnotation(fieldName));
                break;

            case ONE_TO_MANY:
                fieldBuilder.addAnnotation(createOneToManyAnnotation());
                break;

            case MANY_TO_ONE:
                fieldBuilder.addAnnotation(createManyToOneAnnotation());
                fieldBuilder.addAnnotation(createJoinColumnAnnotation(fieldName));
                break;

            case MANY_TO_MANY:
                fieldBuilder.addAnnotation(createManyToManyAnnotation());
                fieldBuilder.addAnnotation(createJoinTableAnnotation(fieldName));
                break;
        }
    }

    /**
     * Добавляет аннотацию @Column для базовых полей
     */
    private static void addColumnAnnotation(FieldSpec.Builder fieldBuilder, String fieldName) {
        fieldBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Column"))
                .addMember("name", "$S", StringUtils.toSnakeCase(fieldName))
                .build());
    }

    /**
     * Создает аннотацию @Entity
     */
    private static AnnotationSpec createEntityAnnotation() {
        return AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Entity")).build();
    }

    /**
     * Создает аннотацию @Id
     */
    private static AnnotationSpec createIdAnnotation() {
        return AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Id")).build();
    }

    /**
     * Создает аннотацию @GeneratedValue
     */
    private static AnnotationSpec createGeneratedValueAnnotation() {
        return AnnotationSpec.builder(ClassName.get("jakarta.persistence", "GeneratedValue"))
                .addMember("strategy", "$T.IDENTITY",
                        ClassName.get("jakarta.persistence", "GenerationType"))
                .build();
    }

    /**
     * Создает аннотацию @OneToOne
     */
    private static AnnotationSpec createOneToOneAnnotation() {
        return AnnotationSpec.builder(ClassName.get("jakarta.persistence", "OneToOne")).build();
    }

    /**
     * Создает аннотацию @OneToMany
     */
    private static AnnotationSpec createOneToManyAnnotation() {
        return AnnotationSpec.builder(ClassName.get("jakarta.persistence", "OneToMany"))
                .addMember("cascade", "$T.ALL", ClassName.get("jakarta.persistence", "CascadeType"))
                .addMember("fetch", "$T.LAZY", ClassName.get("jakarta.persistence", "FetchType"))
                .build();
    }

    /**
     * Создает аннотацию @ManyToOne
     */
    private static AnnotationSpec createManyToOneAnnotation() {
        return AnnotationSpec.builder(ClassName.get("jakarta.persistence", "ManyToOne"))
                .addMember("fetch", "$T.LAZY", ClassName.get("jakarta.persistence", "FetchType"))
                .build();
    }

    /**
     * Создает аннотацию @ManyToMany
     */
    private static AnnotationSpec createManyToManyAnnotation() {
        return AnnotationSpec.builder(ClassName.get("jakarta.persistence", "ManyToMany")).build();
    }

    /**
     * Создает аннотацию @JoinColumn
     */
    private static AnnotationSpec createJoinColumnAnnotation(String fieldName) {
        return AnnotationSpec.builder(ClassName.get("jakarta.persistence", "JoinColumn"))
                .addMember("name", "$S", StringUtils.toSnakeCase(fieldName) + "_id")
                .build();
    }

    /**
     * Создает аннотацию @JoinTable для ManyToMany отношений
     */
    private static AnnotationSpec createJoinTableAnnotation(String fieldName) {
        return AnnotationSpec.builder(ClassName.get("jakarta.persistence", "JoinTable"))
                .addMember("name", "$S", StringUtils.toSnakeCase(fieldName) + "_mapping")
                .addMember("joinColumns", "@JoinColumn(name = \"$L_id\")", StringUtils.toSnakeCase(fieldName))
                .addMember("inverseJoinColumns", "@JoinColumn(name = \"related_entity_id\")")
                .build();
    }

    /**
     * Создает аннотацию Lombok @Data
     */
    private static AnnotationSpec createLombokDataAnnotation() {
        return AnnotationSpec.builder(ClassName.get("lombok", "Data")).build();
    }

    /**
     * Создает аннотацию Lombok @NoArgsConstructor
     */
    private static AnnotationSpec createLombokNoArgsConstructorAnnotation() {
        return AnnotationSpec.builder(ClassName.get("lombok", "NoArgsConstructor")).build();
    }

    /**
     * Создает аннотацию Lombok @AllArgsConstructor
     */
    private static AnnotationSpec createLombokAllArgsConstructorAnnotation() {
        return AnnotationSpec.builder(ClassName.get("lombok", "AllArgsConstructor")).build();
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