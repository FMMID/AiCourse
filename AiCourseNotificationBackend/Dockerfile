FROM gradle:8.5-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon || gradle build --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Обрати внимание: путь может отличаться в зависимости от того, как собирает gradle
# Обычно это build/libs/имя-проекта-all.jar.
# Звездочка * поможет найти любой jar.
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]