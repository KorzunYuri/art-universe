package yurykorzun.art.universe.common.observability.config;

import io.micrometer.observation.ObservationPredicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
}
