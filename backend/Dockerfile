FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B test package -DskipTests=false

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=build /app/target/*.jar app.jar
ENV H2_DATA_PATH=/app/data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
