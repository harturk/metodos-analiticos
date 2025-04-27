# Use a Maven image to build the app
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

# Use a smaller Java image for running
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/metodos-t1-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]