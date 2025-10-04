package yurykorzun.art.universe.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Common web configuration that automatically enables common exception handling
 * and other shared web components when imported by modules.
 */
@Configuration
public class CommonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // don't change the name of boolean properties
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy() {
            @Override
            public String nameForGetterMethod(MapperConfig<?> config,
                                              AnnotatedMethod method,
                                              String defaultName) {
                String methodName = method.getName();
                if (methodName.startsWith("is") && method.getRawReturnType().equals(boolean.class)) {
                    return methodName;
                }
                return super.nameForGetterMethod(config, method, defaultName);
            }
        });
        return mapper;
    }
}
