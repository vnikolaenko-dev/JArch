package github.vnikolaenko.jarch.generator.generator.included;

import com.squareup.javapoet.*;
import github.vnikolaenko.jarch.generator.utils.StringUtils;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Генератор Service слоя приложения с правильным внедрением зависимостей
 */
public class ServiceGenerator {

    /**
     * Генерирует Service интерфейс и его реализацию
     */
    public static void generateService(String basePackage, String entityName, Path targetDir) throws IOException {
        // Генерируем интерфейс сервиса
        generateServiceInterface(basePackage, entityName, targetDir);

        // Генерируем реализацию сервиса
        generateServiceImpl(basePackage, entityName, targetDir);
    }

    /**
     * Генерирует интерфейс сервиса
     */
    private static void generateServiceInterface(String basePackage, String entityName, Path targetDir) throws IOException {
        String interfaceName = StringUtils.capitalizeFirst(entityName) + "Service";
        String dtoName = StringUtils.capitalizeFirst(entityName) + "DTO";

        ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
        ClassName listClass = ClassName.get("java.util", "List");

        // Создаем интерфейс с CRUD методами
        TypeSpec serviceInterface = TypeSpec.interfaceBuilder(interfaceName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(createFindAllMethod(listClass, dtoClass))
                .addMethod(createFindByIdMethod(dtoClass))
                .addMethod(createSaveMethod(dtoClass))
                .addMethod(createUpdateMethod(dtoClass))
                .addMethod(createDeleteMethod())
                .build();

        // Записываем сгенерированный интерфейс
        writeJavaFile(basePackage + ".service", serviceInterface, targetDir);
    }

    /**
     * Генерирует реализацию сервиса с правильными аннотациями
     */
    private static void generateServiceImpl(String basePackage, String entityName, Path targetDir) throws IOException {
        String className = StringUtils.capitalizeFirst(entityName) + "ServiceImpl";
        String interfaceName = StringUtils.capitalizeFirst(entityName) + "Service";
        String dtoName = StringUtils.capitalizeFirst(entityName) + "DTO";
        String entityClassName = StringUtils.capitalizeFirst(entityName);
        String repositoryName = StringUtils.capitalizeFirst(entityName) + "Repository";

        // Импорты классов
        ClassName entityClass = ClassName.get(basePackage + ".model", entityClassName);
        ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
        ClassName repositoryClass = ClassName.get(basePackage + ".repository", repositoryName);
        ClassName serviceInterface = ClassName.get(basePackage + ".service", interfaceName);

        // Создаем класс реализации сервиса
        TypeSpec serviceImpl = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createServiceAnnotation()) // @Service
                .addAnnotation(createTransactionalAnnotation()) // @Transactional
                .addAnnotation(createRequiredArgsConstructorAnnotation()) // @RequiredArgsConstructor
                .addSuperinterface(serviceInterface)
                .addField(createRepositoryField(repositoryClass))
                .addField(createModelMapperField())
                .addMethod(createFindAllMethodImpl(entityClass, dtoClass))
                .addMethod(createFindByIdMethodImpl(entityClass, dtoClass))
                .addMethod(createSaveMethodImpl(entityClass, dtoClass))
                .addMethod(createUpdateMethodImpl(entityClass, dtoClass))
                .addMethod(createDeleteMethodImpl(entityClass))
                .build();

        // Записываем сгенерированную реализацию
        writeJavaFile(basePackage + ".service", serviceImpl, targetDir);
    }

    /**
     * Создает поле репозитория с аннотацией @Autowired
     */
    private static FieldSpec createRepositoryField(ClassName repositoryClass) {
        return FieldSpec.builder(repositoryClass, "repository", Modifier.PRIVATE, Modifier.FINAL)
                .addAnnotation(createAutowiredAnnotation())
                .build();
    }

    /**
     * Создает поле ModelMapper с аннотацией @Autowired
     */
    private static FieldSpec createModelMapperField() {
        return FieldSpec.builder(ClassName.get("org.modelmapper", "ModelMapper"),
                        "modelMapper", Modifier.PRIVATE, Modifier.FINAL)
                .addAnnotation(createAutowiredAnnotation())
                .build();
    }

    /**
     * Создает аннотацию @Service
     */
    private static AnnotationSpec createServiceAnnotation() {
        return AnnotationSpec.builder(ClassName.get("org.springframework.stereotype", "Service"))
                .build();
    }

    /**
     * Создает аннотацию @Transactional
     */
    private static AnnotationSpec createTransactionalAnnotation() {
        return AnnotationSpec.builder(ClassName.get("org.springframework.transaction.annotation", "Transactional"))
                .build();
    }

    /**
     * Создает аннотацию @Autowired
     */
    private static AnnotationSpec createAutowiredAnnotation() {
        return AnnotationSpec.builder(ClassName.get("org.springframework.beans.factory.annotation", "Autowired"))
                .build();
    }

    /**
     * Создает аннотацию Lombok @RequiredArgsConstructor
     */
    private static AnnotationSpec createRequiredArgsConstructorAnnotation() {
        return AnnotationSpec.builder(ClassName.get("lombok", "RequiredArgsConstructor"))
                .build();
    }

    // Методы для создания методов интерфейса сервиса (без изменений)
    private static MethodSpec createFindAllMethod(ClassName listClass, ClassName dtoClass) {
        return MethodSpec.methodBuilder("findAll")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(ParameterizedTypeName.get(listClass, dtoClass))
                .build();
    }

    private static MethodSpec createFindByIdMethod(ClassName dtoClass) {
        return MethodSpec.methodBuilder("findById")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(ClassName.get(Long.class), "id")
                .returns(dtoClass)
                .build();
    }

    private static MethodSpec createSaveMethod(ClassName dtoClass) {
        return MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(dtoClass, "dto")
                .returns(dtoClass)
                .build();
    }

    private static MethodSpec createUpdateMethod(ClassName dtoClass) {
        return MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(ClassName.get(Long.class), "id")
                .addParameter(dtoClass, "dto")
                .returns(dtoClass)
                .build();
    }

    private static MethodSpec createDeleteMethod() {
        return MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(ClassName.get(Long.class), "id")
                .build();
    }

    // Методы реализации сервиса (без изменений)
    private static MethodSpec createFindAllMethodImpl(ClassName entityClass, ClassName dtoClass) {
        return MethodSpec.methodBuilder("findAll")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createTransactionalReadOnlyAnnotation())
                .returns(ParameterizedTypeName.get(ClassName.get("java.util", "List"), dtoClass))
                .addStatement("$T<$T> entities = repository.findAll()",
                        ClassName.get("java.util", "List"), entityClass)
                .addStatement("return entities.stream()\n" +
                                ".map(entity -> modelMapper.map(entity, $T.class))\n" +
                                ".collect($T.toList())", dtoClass,
                        ClassName.get("java.util.stream", "Collectors"))
                .build();
    }

    private static MethodSpec createFindByIdMethodImpl(ClassName entityClass, ClassName dtoClass) {
        return MethodSpec.methodBuilder("findById")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createTransactionalReadOnlyAnnotation())
                .addParameter(ClassName.get(Long.class), "id")
                .returns(dtoClass)
                .addStatement("$T<$T> entity = repository.findById(id)",
                        ClassName.get("java.util", "Optional"), entityClass)
                .addCode("""
                                if (entity.isPresent()) {
                                    return modelMapper.map(entity.get(), $T.class);
                                } else {
                                    throw new $T($S + id);
                                }
                                """, dtoClass,
                        ClassName.get("jakarta.persistence", "EntityNotFoundException"),
                        "Сущность не найдена с id: ")
                .build();
    }


    private static MethodSpec createSaveMethodImpl(ClassName entityClass, ClassName dtoClass) {
        return MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createTransactionalAnnotation())
                .addParameter(dtoClass, "dto")
                .returns(dtoClass)
                .addStatement("$T entity = modelMapper.map(dto, $T.class)", entityClass, entityClass)
                .addStatement("$T savedEntity = repository.save(entity)", entityClass)
                .addStatement("return modelMapper.map(savedEntity, $T.class)", dtoClass)
                .build();
    }

    private static MethodSpec createUpdateMethodImpl(ClassName entityClass, ClassName dtoClass) {
        return MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createTransactionalAnnotation())
                .addParameter(ClassName.get(Long.class), "id")
                .addParameter(dtoClass, "dto")
                .returns(dtoClass)
                .addStatement("$T<$T> existingEntity = repository.findById(id)",
                        ClassName.get("java.util", "Optional"), entityClass)
                .addCode("""
                                if (existingEntity.isPresent()) {
                                    $T entity = existingEntity.get();
                                    modelMapper.map(dto, entity);
                                    $T updatedEntity = repository.save(entity);
                                    return modelMapper.map(updatedEntity, $T.class);
                                } else {
                                    throw new $T($S + id);
                                }
                                """, entityClass, entityClass, dtoClass,
                        ClassName.get("jakarta.persistence", "EntityNotFoundException"),
                        "Сущность не найдена с id: ")
                .build();
    }


    private static MethodSpec createDeleteMethodImpl(ClassName entityClass) {
        return MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createTransactionalAnnotation())
                .addParameter(ClassName.get(Long.class), "id")
                .addStatement("$T<$T> entity = repository.findById(id)",
                        ClassName.get("java.util", "Optional"), entityClass)
                .addStatement("if (entity.isPresent()) {\n" +
                                "repository.deleteById(id);\n" +
                                "} else {\n" +
                                "throw new $T($S + id);\n" +
                                "}",
                        ClassName.get("jakarta.persistence", "EntityNotFoundException"),
                        "Сущность не найдена с id: ")
                .build();
    }

    /**
     * Создает аннотацию @Transactional(readOnly = true)
     */
    private static AnnotationSpec createTransactionalReadOnlyAnnotation() {
        return AnnotationSpec.builder(ClassName.get("org.springframework.transaction.annotation", "Transactional"))
                .addMember("readOnly", "$L", "true")
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