# ЭТАП 1: Сборка (Обновили версию Gradle до 8.11+)
FROM gradle:8.11.1-jdk17 AS build
WORKDIR /app

# Копируем конфиги
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Копируем исходники
COPY src ./src

# Собираем Fat Jar
# ВАЖНО: Если скрипт gradlew не имеет прав на запуск, можно использовать системный gradle
RUN gradle shadowJar --no-daemon -x test

# ЭТАП 2: Запуск (Тут все без изменений)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*-all.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]