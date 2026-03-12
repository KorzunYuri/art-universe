FROM eclipse-temurin:21-jre
WORKDIR /app

COPY build/libs/*.jar app.jar
COPY build/libs/lib/ lib/
COPY build/libs/resources/ resources/

ENTRYPOINT ["java", "-jar", "app.jar"]
