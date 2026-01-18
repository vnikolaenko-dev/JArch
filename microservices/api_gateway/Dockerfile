# Используем Maven для сборки
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Копируем pom.xml для кэширования зависимостей
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем
COPY src ./src
RUN mvn clean package -DskipTests

# Финальный образ
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Создаем пользователя для безопасности
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Копируем jar файл
COPY --from=build app/target/*.jar app.jar

# Настройки
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]