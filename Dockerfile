# syntax=docker/dockerfile:1

FROM openjdk:11-jdk-slim AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw ./
RUN chmod +x mvnw
COPY pom.xml ./
COPY src ./src
RUN ./mvnw clean package

FROM openjdk:11-jre-slim
COPY --from=build /app/target/*.jar /app/retroboard.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/retroboard.jar"]
