# Étape 1 : Build du frontend React
FROM node:18-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ .
RUN npm run build-docker

# Étape 2 : Build backend Spring Boot avec Gradle
FROM gradle:8.3-jdk17 AS backend-build
WORKDIR /app
COPY backend/ .
COPY --from=frontend-build /app/dist /app/src/main/resources/static
RUN gradle build --no-daemon

# Étape 3 : Image finale
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=backend-build /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
