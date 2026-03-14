package yurykorzun.art.universe.common.config.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import yurykorzun.art.universe.common.config.client.ConfigClientProperties;
import yurykorzun.art.universe.common.config.client.ConfigPropertyAutoRegistrator;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.config.client.ConfigServiceClient;

/**
 * Auto-configures the dynamic config client.
 * <p>
 * Activated only when {@code au.config.client.service-url} is set in the application's
 * {@code application.yml}. Modules that do not declare this property get zero overhead.
 */
@Profile("!test")
@AutoConfiguration
@ConditionalOnProperty(name = "au.config.client.service-url")
@EnableConfigurationProperties(ConfigClientProperties.class)
public class CommonsConfigClientAutoConfiguration {

    @Bean
    public ConfigServiceClient configServiceClient(ConfigClientProperties properties, ObjectMapper objectMapper) {
        return new ConfigServiceClient(properties, objectMapper);
    }

    @Bean
    public ConfigPropertyAutoRegistrator configPropertyAutoRegistrator(
        ConfigServiceClient client,
        ConfigClientProperties properties
    ) {
        return new ConfigPropertyAutoRegistrator(client, properties);
    }

    @Bean
    public ConfigPropertyHolder configPropertyHolder(ConfigPropertyAutoRegistrator registrator) {
        return registrator.getHolder();
    }
}
