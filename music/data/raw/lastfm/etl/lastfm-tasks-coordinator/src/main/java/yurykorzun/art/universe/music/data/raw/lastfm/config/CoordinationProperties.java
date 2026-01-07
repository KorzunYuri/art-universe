package yurykorzun.art.universe.music.data.raw.lastfm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for task coordination.
 * Provides default values that can be overridden via application.yml or environment variables.
 */
@Data
@ConfigurationProperties(prefix = "lastfm.coordination")
public class CoordinationProperties {

    /**
     * Coordination provider type: "in-memory" or "database"
     * Default: "database" for distributed coordination
     */
    private String provider = "database";

    private Heartbeat heartbeat = new Heartbeat();
    private Cleanup cleanup = new Cleanup();

    @Data
    public static class Heartbeat {
        /**
         * How often to send heartbeat (seconds)
         */
        private int intervalSeconds = 30;

        /**
         * When to consider instance crashed (seconds)
         */
        private int staleTimeoutSeconds = 120;
    }

    @Data
    public static class Cleanup {
        /**
         * How often to cleanup stale instances (seconds)
         */
        private int intervalSeconds = 60;
    }
}
