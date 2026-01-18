package vnikolaenko.github.jarch.generator.generator.included;

import com.squareup.javapoet.*;
import vnikolaenko.github.jarch.generator.utils.StringUtils;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Генератор REST контроллеров для Spring приложения
 * Отвечает за создание классов контроллеров с CRUD операциями
 */
public class ControllerGenerator {

    /**
     * Генерирует REST контроллер для сущности
     */
    public static void generateController(String basePackage, String entityName, Path targetDir) throws IOException {
        String className = StringUtils.capitalizeFirst(entityName) + "Controller";
        String serviceName = StringUtils.capitalizeFirst(entityName) + "Service";
        String dtoName = StringUtils.capitalizeFirst(entityName) + "DTO";
        String serviceFieldName = StringUtils.getServiceFieldName(entityName);

        // Импорты классов
        ClassName serviceClass = ClassName.get(basePackage + ".service", serviceName);
        ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
        ClassName listClass = ClassName.get("java.util", "List");
        ClassName responseEntityClass = ClassName.get("org.springframework.http", "ResponseEntity");

        // Создание методов контроллера
        MethodSpec getAll = createGetAllMethod(serviceFieldName, listClass, dtoClass);
        MethodSpec getById = createGetByIdMethod(serviceFieldName, dtoClass);
        MethodSpec create = createCreateMethod(serviceFieldName, dtoClass);
        MethodSpec update = createUpdateMethod(serviceFieldName, dtoClass);
        MethodSpec delete = createDeleteMethod(serviceFieldName, responseEntityClass);

        // Создание класса контроллера
        TypeSpec controller = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createRestControllerAnnotation())
                .addAnnotation(createRequestMappingAnnotation(entityName))
                .addAnnotation(createRequiredArgsConstructorAnnotation()) // Добавляем Lombok аннотацию
                .addField(createServiceField(serviceClass, serviceFieldName))
                .addMethod(getAll)
                .addMethod(getById)
                .addMethod(create)
                .addMethod(update)
                .addMethod(delete)
                .build();

        // Запись сгенерированного файла
        writeJavaFile(basePackage + ".controller", controller, targetDir);
    }

    /**
     * Создает метод для удаления записи (исправленный)
     */
    private static MethodSpec createDeleteMethod(String serviceFieldName, ClassName responseEntityClass) {
        return MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(ClassName.get("org.springframework.web.bind.annotation", "DeleteMapping"))
                        .addMember("value", "$S", "/{id}")
                        .build())
                .addParameter(createIdParameter())
                .returns(ParameterizedTypeName.get(responseEntityClass, ClassName.get(Void.class)))
                .addStatement("$L.delete(id)", serviceFieldName)
                .addStatement("return $T.ok().build()", responseEntityClass)
                .build();
    }

    /**
     * Создает метод для получения всех записей
     */
    private static MethodSpec createGetAllMethod(String serviceFieldName, ClassName listClass, ClassName dtoClass) {
        return MethodSpec.methodBuilder("getAll")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(ClassName.get("org.springframework.web.bind.annotation", "GetMapping"))
                        .build())
                .returns(ParameterizedTypeName.get(listClass, dtoClass))
                .addStatement("return $L.findAll()", serviceFieldName)
                .build();
    }

    /**
     * Создает метод для получения записи по ID
     */
    private static MethodSpec createGetByIdMethod(String serviceFieldName, ClassName dtoClass) {
        return MethodSpec.methodBuilder("getById")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(ClassName.get("org.springframework.web.bind.annotation", "GetMapping"))
                        .addMember("value", "$S", "/{id}")
                        .build())
                .addParameter(createIdParameter())
                .returns(dtoClass)
                .addStatement("return $L.findById(id)", serviceFieldName)
                .build();
    }

    /**
     * Создает метод для создания новой записи
     */
    private static MethodSpec createCreateMethod(String serviceFieldName, ClassName dtoClass) {
        return MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(ClassName.get("org.springframework.web.bind.annotation", "PostMapping"))
                        .build())
                .addParameter(createDtoParameter(dtoClass))
                .returns(dtoClass)
                .addStatement("return $L.save(dto)", serviceFieldName)
                .build();
    }

    /**
     * Создает метод для обновления записи
     */
    private static MethodSpec createUpdateMethod(String serviceFieldName, ClassName dtoClass) {
        return MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(ClassName.get("org.springframework.web.bind.annotation", "PutMapping"))
                        .addMember("value", "$S", "/{id}")
                        .build())
                .addParameter(createIdParameter())
                .addParameter(createDtoParameter(dtoClass))
                .returns(dtoClass)
                .addStatement("return $L.update(id, dto)", serviceFieldName)
                .build();
    }

    /**
     * Создает параметр ID для методов
     */
    private static ParameterSpec createIdParameter() {
        return ParameterSpec.builder(Long.class, "id")
                .addAnnotation(ClassName.get("org.springframework.web.bind.annotation", "PathVariable"))
                .build();
    }

    /**
     * Создает параметр DTO для методов
     */
    private static ParameterSpec createDtoParameter(ClassName dtoClass) {
        return ParameterSpec.builder(dtoClass, "dto")
                .addAnnotation(ClassName.get("org.springframework.web.bind.annotation", "RequestBody"))
                .build();
    }

    /**
     * Создает аннотацию RestController
     */
    private static AnnotationSpec createRestControllerAnnotation() {
        return AnnotationSpec.builder(ClassName.get("org.springframework.web.bind.annotation", "RestController"))
                .build();
    }

    /**
     * Создает аннотацию RequestMapping с базовым путем
     */
    private static AnnotationSpec createRequestMappingAnnotation(String entityName) {
        return AnnotationSpec.builder(ClassName.get("org.springframework.web.bind.annotation", "RequestMapping"))
                .addMember("value", "$S", "/api/" + StringUtils.capitalizeFirst(entityName) + "s")
                .build();
    }

    /**
     * Создает аннотацию Lombok @RequiredArgsConstructor
     */
    private static AnnotationSpec createRequiredArgsConstructorAnnotation() {
        return AnnotationSpec.builder(ClassName.get("lombok", "RequiredArgsConstructor"))
                .build();
    }

    /**
     * Создает поле сервиса в контроллере
     */
    private static FieldSpec createServiceField(ClassName serviceClass, String serviceFieldName) {
        return FieldSpec.builder(serviceClass, serviceFieldName, Modifier.PRIVATE, Modifier.FINAL)
                .addAnnotation(createAutowiredAnnotation()) // Добавляем @Autowired
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
     * Записывает сгенерированный Java файл
     */
    private static void writeJavaFile(String packageName, TypeSpec typeSpec, Path targetDir) throws IOException {
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .indent("    ")
                .build();

        javaFile.writeTo(Paths.get(targetDir + "/src/main/java"));
    }
}