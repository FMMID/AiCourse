# ЭТАП 1: Сборка
FROM gradle:8.11.1-jdk17 AS build
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY src ./src

RUN gradle shadowJar --no-daemon -x test

# ЭТАП 2: Запуск (Alpine-based)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Переключаемся на root для установки сертификатов
USER root

# 1. Устанавливаем необходимые пакеты (wget и ca-certificates)
RUN apk update && apk add --no-cache wget ca-certificates

# 2. Скачиваем сертификаты Минцифры
RUN wget https://gu-st.ru/content/lending/russian_trusted_root_ca_pem.crt -O /usr/local/share/ca-certificates/russian_trusted_root_ca.crt && \
    wget https://gu-st.ru/content/lending/russian_trusted_sub_ca_pem.crt -O /usr/local/share/ca-certificates/russian_trusted_sub_ca.crt

# 3. Обновляем системное хранилище сертификатов
RUN update-ca-certificates

# 4. Импортируем сертификаты в хранилище Java (cacerts)
# Пароль по умолчанию для cacerts — 'changeit'
RUN keytool -import -trustcacerts -alias "russian-root-ca" \
    -file /usr/local/share/ca-certificates/russian_trusted_root_ca.crt \
    -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt && \
    keytool -import -trustcacerts -alias "russian-sub-ca" \
    -file /usr/local/share/ca-certificates/russian_trusted_sub_ca.crt \
    -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt

# Копируем собранный jar
COPY --from=build /app/build/libs/*-all.jar app.jar
# Копируем ключ Firebase в контейнер (в папку /app)
COPY firebase-service.json .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]