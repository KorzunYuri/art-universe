FROM openjdk:17-jdk-alpine
WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

ARG JAR_SOURCE=build/libs/*.jar
ARG JAR_TARGET=mu-data-raw-lastfm.jar
COPY ${JAR_SOURCE} mu-data-raw-lastfm.jar
EXPOSE 8080
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-XX:InitialRAMPercentage=50.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Dspring.output.ansi.enabled=ALWAYS", \
            "-jar", "mu-data-raw-lastfm.jar"]
