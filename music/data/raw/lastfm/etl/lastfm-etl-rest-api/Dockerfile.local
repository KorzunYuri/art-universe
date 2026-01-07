FROM eclipse-temurin:21-jre
WORKDIR /app

# Install curl for healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Copy pre-extracted layers from build/docker-layers
COPY build/docker-layers/dependencies/ ./dependencies/
COPY build/docker-layers/spring-boot-loader/ ./spring-boot-loader/
COPY build/docker-layers/snapshot-dependencies/ ./snapshot-dependencies/
COPY build/docker-layers/application/ ./application/

EXPOSE 8080

# Run the application with proper classpath (thin JAR needs dependencies)
ENTRYPOINT ["java", \
            "-XX:MaxRAMPercentage=75.0", \
            "-XX:InitialRAMPercentage=50.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Dspring.output.ansi.enabled=ALWAYS", \
            "-cp", "application/*:application/lib/*:dependencies/lib/*", \
            "yurykorzun.art.universe.music.data.raw.lastfm.LastfmEtlRestApiApplication"]
