FROM openjdk:17-jdk-alpine
WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

ARG JAR_SOURCE=build/libs/*.jar
ARG JAR_TARGET=mu-data-raw-lastfm.jar
COPY ${JAR_SOURCE} mu-data-raw-lastfm.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "mu-data-raw-lastfm.jar"]
