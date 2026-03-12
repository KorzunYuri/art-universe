package yurykorzun.art.universe.common.config.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "au.config.client")
public class ConfigClientProperties {

    /**
     * Base URL of the config service REST API, e.g. {@code http://au-config-service:8080}.
     * Setting this property activates the config client auto-configuration.
     */
    private String serviceUrl;

    /**
     * How often (in seconds) to refresh property values from the config service.
     * Default: 5 seconds.
     */
    private long refreshIntervalSeconds = 5;

    /**
     * HTTP connection timeout in milliseconds. Default: 3000.
     */
    private int connectTimeoutMs = 3000;

    /**
     * HTTP read timeout in milliseconds. Default: 5000.
     */
    private int readTimeoutMs = 5000;

}
