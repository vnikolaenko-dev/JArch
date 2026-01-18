package vnikolaenko.github.jarch.generator.utils;

/**
 * Утилитарный класс для работы со строками
 * Содержит вспомогательные методы для преобразования строк
 */
public class StringUtils {

    /**
     * Преобразует первую букву строки в верхний регистр
     *
     * @param str исходная строка
     * @return строка с первой буквой в верхнем регистре
     */
    public static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Преобразует строку в snake_case формат
     *
     * @param str исходная строка
     * @return строка в snake_case
     */
    public static String toSnakeCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    /**
     * Генерирует имя поля для сервиса на основе имени сущности
     *
     * @param entityName имя сущности
     * @return имя поля сервиса
     */
    public static String getServiceFieldName(String entityName) {
        return entityName.substring(0, 1).toLowerCase() + entityName.substring(1) + "Service";
    }
}