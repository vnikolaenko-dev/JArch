package vnikolaenko.github.jarch.generator.generator.included;

import com.squareup.javapoet.*;
import vnikolaenko.github.jarch.generator.auxiliary.Field;
import vnikolaenko.github.jarch.generator.utils.StringUtils;
import vnikolaenko.github.jarch.generator.utils.TypeMapper;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Генератор Repository интерфейсов для Spring Data JPA
 * Отвечает за создание репозиториев для доступа к данным
 */
public class RepositoryGenerator {

    /**
     * Генерирует Repository интерфейс для сущности
     *
     * @param basePackage базовый пакет приложения
     * @param entityName имя сущности
     * @param fields список полей сущности
     */
    public static void generateRepository(String basePackage, String entityName, List<Field> fields, Path targetDir) throws IOException {
        String className = StringUtils.capitalizeFirst(entityName) + "Repository";
        String entityClassName = StringUtils.capitalizeFirst(entityName);

        // Импорты классов
        ClassName entityClass = ClassName.get(basePackage + ".model", entityClassName);
        ClassName longClass = ClassName.get(Long.class);

        // Создаем билдер для интерфейса репозитория
        TypeSpec.Builder repositoryBuilder = TypeSpec.interfaceBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(createJpaRepositoryInterface(entityClass, longClass))
                .addSuperinterface(createJpaSpecificationExecutorInterface());

        // Добавляем кастомные методы поиска, если есть поля
        if (!fields.isEmpty()) {
            repositoryBuilder.addMethod(createFindByMethod(fields.get(0), entityClass));
        }

        TypeSpec repository = repositoryBuilder.build();

        // Записываем сгенерированный файл
        writeJavaFile(basePackage + ".repository", repository, targetDir);
    }

    /**
     * Создает интерфейс JpaRepository с указанными generic типами
     */
    private static ParameterizedTypeName createJpaRepositoryInterface(ClassName entityClass, ClassName idClass) {
        return ParameterizedTypeName.get(
                ClassName.get("org.springframework.data.jpa.repository", "JpaRepository"),
                entityClass, idClass);
    }

    /**
     * Создает интерфейс JpaSpecificationExecutor для поддержки спецификаций
     */
    private static TypeName createJpaSpecificationExecutorInterface() {
        return ClassName.get("org.springframework.data.jpa.repository", "JpaSpecificationExecutor");
    }

    /**
     * Создает кастомный метод поиска по первому полю
     */
    private static MethodSpec createFindByMethod(Field field, ClassName entityClass) {
        String fieldName = field.getFieldName();
        String methodName = "findBy" + StringUtils.capitalizeFirst(fieldName);

        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(ParameterizedTypeName.get(
                        ClassName.get("java.util", "List"),
                        entityClass))
                .addParameter(TypeMapper.getJavaType(field.getFieldType(), ""), fieldName)
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