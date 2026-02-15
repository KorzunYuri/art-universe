package yurykorzun.art.universe.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Common configuration that provides common components and customizations used across the project when imported by modules.
 */
@AutoConfiguration
public class CommonConfig {

    /**
     * Registers a shared ObjectMapper bean following project serialization rules:
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return getObjectMapper();
    }

    /**
     * Returns ObjectMapper following project serialization rules:
     * <ul>
     *   <li>Dates/times serialized as ISO-8601 strings (not timestamps)</li>
     *   <li>Boolean getters with {@code is} prefix keep their name (e.g. {@code isSomething}, not {@code something})</li>
     * </ul>
     */
    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(getObjectMapperPropertyNamingStrategy());
        return mapper;
    }

    public static PropertyNamingStrategy getObjectMapperPropertyNamingStrategy() {
        return new PropertyNamingStrategy() {
            @Override
            public String nameForGetterMethod(MapperConfig<?> config,
                                              AnnotatedMethod method,
                                              String defaultName) {
                // don't change name of boolean properties
                String methodName = method.getName();
                if (methodName.startsWith("is") && method.getRawReturnType().equals(boolean.class)) {
                    return methodName;
                }
                return super.nameForGetterMethod(config, method, defaultName);
            }
        };
    }
}
