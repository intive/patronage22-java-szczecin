# syntax=docker/dockerfile:1

FROM openjdk:11-jre-slim AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw ./
RUN chmod +x mvnw
COPY pom.xml ./
COPY checkstyle.xml ./
COPY src ./src
RUN ./mvnw clean package -DskipTests
ARG JAR_FILE=*.jar

FROM openjdk:11-jre-slim
COPY --from=build /app/target/*.jar /app/retroboard.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/retroboard.jar"]