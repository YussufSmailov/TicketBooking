# Сборка: Maven собирает jar (тесты не запускаются — им нужна локальная база)
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# Запуск: только Java и готовый jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
CMD ["sh", "-c", "java -XX:MaxRAMPercentage=75 -jar app.jar"]
