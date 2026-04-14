# Stage 1 — Build the fat JAR
FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle fatJar --no-daemon

# Stage 2 — Run the JAR
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/notebook-api.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
