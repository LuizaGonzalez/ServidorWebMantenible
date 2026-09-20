# Etapa 1: compilar el proyecto
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: imagen final, solo con el jar ya compilado
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/ServidorWebMantenible-1.0-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]