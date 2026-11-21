FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress verify

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/olympic-nutrition-tracker-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/docker/render-entrypoint.sh /app/render-entrypoint.sh
USER 10001:10001
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 10000
ENTRYPOINT ["/bin/sh", "/app/render-entrypoint.sh", "java", "-jar", "app.jar"]
