FROM openjdk:17-jdk-alpine
WORKDIR /app
ARG JAR_SOURCE=build/libs/*.jar
ARG JAR_TARGET=mu-data.jar
COPY ${JAR_SOURCE} mu-data.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "mu-data.jar"]
