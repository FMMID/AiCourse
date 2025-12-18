FROM gradle:8.11.1-jdk17 AS build

WORKDIR /app

# Копируем файлы конфигурации Gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
# Копируем папку gradle (если она есть, иначе эта строка не критична, если gradle wrapper не используется внутри)
COPY gradle ./gradle
COPY src ./src

# Собираем
RUN gradle buildFatJar --no-daemon || gradle build --no-daemon

# ЭТАП 2: Запуск
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]