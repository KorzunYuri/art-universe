package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Generates unique instance identifiers for coordination purposes.
 * Format: {module-name}--{hostname}--{pid}--{timestamp}
 * <p>
 * Uses double-dash (--) as separator to avoid conflicts with dashes in module names.
 */
public class InstanceIdGenerator {

    private InstanceIdGenerator() {
        // Utility class
    }

    /**
     * Generates a unique instance ID.
     *
     * @param moduleName name of the module (e.g., "lastfm-etl-rest-api")
     * @return unique instance identifier
     */
    public static String generate(String moduleName) {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            long timestamp = System.currentTimeMillis();

            return String.format("%s--%s--%s--%d",
                    sanitize(moduleName),
                    sanitize(hostname),
                    pid,
                    timestamp
            );
        } catch (Exception e) {
            // Fallback to UUID-based ID if hostname/pid retrieval fails
            return moduleName + "--" + UUID.randomUUID();
        }
    }

    private static String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9-]", "").toLowerCase();
    }
}
