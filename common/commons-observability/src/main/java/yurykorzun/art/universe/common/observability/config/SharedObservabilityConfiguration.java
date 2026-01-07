package yurykorzun.art.universe.common.observability.config;

import io.micrometer.observation.ObservationPredicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Autoconfiguration for observability filtering.
 * The purpose is to reduce trace noise and focus tracing on business operations only.
 */
@AutoConfiguration
@Slf4j
public class SharedObservabilityConfiguration {

    public static final String SCHEDULED_OBSERVATION_FILTER_BEAN_NAME = "scheduledObservationFilter";
    private static final String ACTUATOR_OBSERVATION_FILTER_BEAN_NAME = "actuatorObservationFilter";

    /**
     * Filters out automatic observations for @Scheduled methods.
     *
     * @return predicate that returns false for scheduled task observations
     */
    @Bean
    @ConditionalOnMissingBean(name = SCHEDULED_OBSERVATION_FILTER_BEAN_NAME)
    public ObservationPredicate skipScheduledTasksObservationPredicate() {
        return (name, context) -> {
            // These have names like "task my-bean-name.fixed-delay"
            if (name.startsWith("tasks.scheduled.execution")) {
                log.trace("Skipping observation for scheduled task: {}", name);
                return false;
            }
            return true;
        };
    }

    /**
     * Filters out observations for HTTP requests to actuator endpoints.
     *
     * <p>Only active when Spring Web is on the classpath (conditional on ServerRequestObservationContext).</p>
     *
     * @return predicate that returns false for actuator endpoint observations
     */
    @Bean
    @ConditionalOnMissingBean(name = ACTUATOR_OBSERVATION_FILTER_BEAN_NAME)
    @ConditionalOnClass(name = "org.springframework.http.server.observation.ServerRequestObservationContext")
    public ObservationPredicate skipActuatorEndpointsObservationPredicate() {
        return (name, context) -> {
            // Only process HTTP server request observations
            if (!"http.server.requests".equals(name)) {
                return true;
            }

            try {
                // Use reflection to avoid hard dependency on spring-web
                Class<?> contextClass = context.getClass();
                if (contextClass.getName().contains("ServerRequestObservationContext")) {
                    // Get the carrier (HTTP request) via reflection
                    Object carrier = contextClass.getMethod("getCarrier").invoke(context);
                    if (carrier != null) {
                        // Get request URI
                        Object requestUri = carrier.getClass().getMethod("getRequestURI").invoke(carrier);
                        if (requestUri != null) {
                            String uri = requestUri.toString();
                            if (uri.startsWith("/actuator/")) {
                                log.trace("Skipping observation for actuator endpoint: {}", uri);
                                return false;
                            }
                            if (uri.startsWith("/health")) {
                                log.trace("Skipping observation for health endpoint: {}", uri);
                                return false;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // If reflection fails, allow the observation (fail open)
                log.debug("Failed to check actuator endpoint filter: {}", e.getMessage());
            }

            return true;
        };
    }
}
