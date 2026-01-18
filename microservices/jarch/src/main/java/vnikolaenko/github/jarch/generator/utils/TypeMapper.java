package vnikolaenko.github.jarch.generator.utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Маппер типов для преобразования строковых типов в Java TypeName
 * Обеспечивает соответствие между типами полей и Java типами
 */
public class TypeMapper {

    /**
     * Преобразует строковый тип в соответствующий Java TypeName
     *
     * @param fieldType строковое представление типа
     * @param basePackage базовый пакет приложения
     * @return соответствующий TypeName
     */
    public static TypeName getJavaType(String fieldType, String basePackage) {
        if (fieldType == null || fieldType.isEmpty()) {
            return ClassName.get(String.class);
        }

        String lowerType = fieldType.toLowerCase().trim();

        // Обработка коллекционных типов (List<T>, Set<T>, Collection<T>)
        if (fieldType.contains("<") && fieldType.contains(">")) {
            return parseCollectionType(fieldType, basePackage);
        }

        // Обработка базовых типов
        switch (lowerType) {
            case "string":
            case "text":
            case "varchar":
                return ClassName.get(String.class);

            case "integer":
            case "int":
                return ClassName.get(Integer.class);

            case "long":
                return ClassName.get(Long.class);

            case "double":
                return ClassName.get(Double.class);

            case "float":
                return ClassName.get(Float.class);

            case "boolean":
            case "bool":
                return ClassName.get(Boolean.class);

            case "date":
                return ClassName.get(java.util.Date.class);

            case "datetime":
            case "localdatetime":
                return ClassName.get(java.time.LocalDateTime.class);

            case "localdate":
                return ClassName.get(java.time.LocalDate.class);

            case "localtime":
                return ClassName.get(java.time.LocalTime.class);

            case "bigdecimal":
                return ClassName.get(java.math.BigDecimal.class);

            case "timestamp":
                return ClassName.get(java.sql.Timestamp.class);

            case "list":
                return ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Object.class));

            case "set":
                return ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(Object.class));

            case "collection":
                return ParameterizedTypeName.get(ClassName.get(Collection.class), ClassName.get(Object.class));

            default:
                // Если тип не базовый, предполагаем что это имя класса сущности
                String className = StringUtils.capitalizeFirst(fieldType);
                return ClassName.get(basePackage + ".model", className);
        }
    }

    /**
     * Парсит коллекционные типы (List<T>, Set<T>, Collection<T>)
     */
    private static TypeName parseCollectionType(String fieldType, String basePackage) {
        try {
            // Извлекаем основной тип коллекции и generic параметр
            int genericStart = fieldType.indexOf("<");
            int genericEnd = fieldType.lastIndexOf(">");

            if (genericStart == -1 || genericEnd == -1) {
                return ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Object.class));
            }

            String collectionType = fieldType.substring(0, genericStart).trim();
            String genericType = fieldType.substring(genericStart + 1, genericEnd).trim();

            // Получаем TypeName для generic параметра
            TypeName genericTypeName = getJavaType(genericType, basePackage);

            // Создаем ParameterizedTypeName для коллекции
            switch (collectionType.toLowerCase()) {
                case "list":
                    return ParameterizedTypeName.get(ClassName.get(List.class), genericTypeName);

                case "set":
                    return ParameterizedTypeName.get(ClassName.get(Set.class), genericTypeName);

                case "collection":
                    return ParameterizedTypeName.get(ClassName.get(Collection.class), genericTypeName);

                default:
                    // По умолчанию используем List
                    return ParameterizedTypeName.get(ClassName.get(List.class), genericTypeName);
            }
        } catch (Exception e) {
            // В случае ошибки возвращаем List<Object>
            return ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Object.class));
        }
    }

    /**
     * Получает TypeName для поля сущности с учетом отношений
     * Этот метод используется в ModelGenerator для JPA полей
     */
    public static TypeName getFieldType(String fieldType, String basePackage) {
        return getJavaType(fieldType, basePackage);
    }

    /**
     * Получает TypeName для DTO поля (упрощенная версия без коллекций)
     */
    public static TypeName getDtoFieldType(String fieldType, String basePackage) {
        // Для DTO коллекционные поля преобразуем в List<Long> для ID
        if (fieldType.contains("<") && fieldType.contains(">")) {
            return ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Long.class));
        }

        return getJavaType(fieldType, basePackage);
    }

    /**
     * Проверяет, является ли тип базовым (не требующим импорта из model пакета)
     */
    public static boolean isBasicType(String fieldType) {
        if (fieldType == null) {
            return false;
        }

        String lowerType = fieldType.toLowerCase();

        // Если это коллекционный тип, считаем его НЕ базовым
        if (fieldType.contains("<") && fieldType.contains(">")) {
            return false;
        }

        return lowerType.equals("string") || lowerType.equals("text") || lowerType.equals("varchar") ||
                lowerType.equals("integer") || lowerType.equals("int") ||
                lowerType.equals("long") || lowerType.equals("double") ||
                lowerType.equals("float") || lowerType.equals("boolean") || lowerType.equals("bool") ||
                lowerType.equals("date") || lowerType.equals("datetime") || lowerType.equals("localdatetime") ||
                lowerType.equals("localdate") || lowerType.equals("localtime") ||
                lowerType.equals("bigdecimal") || lowerType.equals("timestamp");
    }

    /**
     * Проверяет, является ли тип коллекционным
     */
    public static boolean isCollectionType(String fieldType) {
        if (fieldType == null) {
            return false;
        }

        String lowerType = fieldType.toLowerCase();
        return lowerType.startsWith("list<") || lowerType.startsWith("set<") ||
                lowerType.startsWith("collection<") || lowerType.equals("list") ||
                lowerType.equals("set") || lowerType.equals("collection");
    }

    /**
     * Извлекает generic тип из коллекционного типа
     * Например: для "List<User>" вернет "User"
     */
    public static String extractGenericType(String collectionType) {
        if (collectionType == null || !collectionType.contains("<")) {
            return "Object";
        }

        try {
            int start = collectionType.indexOf("<") + 1;
            int end = collectionType.lastIndexOf(">");
            return collectionType.substring(start, end).trim();
        } catch (Exception e) {
            return "Object";
        }
    }

    /**
     * Извлекает основной тип коллекции
     * Например: для "List<User>" вернет "List"
     */
    public static String extractCollectionType(String collectionType) {
        if (collectionType == null) {
            return "List";
        }

        if (!collectionType.contains("<")) {
            return collectionType;
        }

        try {
            return collectionType.substring(0, collectionType.indexOf("<")).trim();
        } catch (Exception e) {
            return "List";
        }
    }
}