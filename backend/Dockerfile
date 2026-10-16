# Stage 1: build with Maven
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -q -DskipTests package

# Stage 2: lightweight runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV TZ=America/Bogota
COPY --from=build /app/target/clinic-facade.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
