FROM openjdk:17-jdk-alpine
WORKDIR /app
ARG JAR_SOURCE=build/libs/*.jar
ARG JAR_TARGET=mu-data-raw-lastfm.jar
COPY ${JAR_SOURCE} mu-data-raw-lastfm.jar
ENTRYPOINT ["java", "-jar", "mu-data-raw-lastfm.jar"]
