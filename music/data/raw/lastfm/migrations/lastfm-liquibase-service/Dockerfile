FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built JAR and dependencies
COPY build/libs/*.jar app.jar
COPY build/libs/lib/ lib/
COPY build/libs/resources/ resources/

# Run Liquibase migration
ENTRYPOINT ["java", "-jar", "app.jar"]
