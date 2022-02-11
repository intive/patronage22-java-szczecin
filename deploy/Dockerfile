# syntax=docker/dockerfile:1

FROM openjdk:11-jdk-slim AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw ./
RUN chmod +x mvnw
COPY pom.xml ./
COPY checkstyle.xml ./
COPY src ./src
COPY .env ./
RUN ./mvnw clean package

FROM openjdk:11-jre-slim
COPY --from=build /app/target/*.jar /app/retroboard.jar
EXPOSE $PORT
ENTRYPOINT ["java","-jar","/app/retroboard.jar"]