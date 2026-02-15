package yurykorzun.art.universe.common.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import yurykorzun.art.universe.common.config.CommonConfig;
import yurykorzun.art.universe.common.web.exception.CommonGlobalExceptionHandler;

/**
 * Common web configuration that automatically enables common exception handling
 * and other shared web components when imported by modules.
 * <p>
 * Registers a {@link MappingJackson2HttpMessageConverter} backed by the project's
 * standard {@link ObjectMapper} (from {@link CommonConfig}), ensuring that REST API
 * endpoints follow the same serialization rules as the shared ObjectMapper bean:
 * ISO-8601 dates and preserved {@code is}-prefixed boolean property names.
 */
@AutoConfiguration
@AutoConfigureAfter(CommonConfig.class)
@AutoConfigureBefore(HttpMessageConvertersAutoConfiguration.class)
@Import({
    CommonGlobalExceptionHandler.class
})
public class CommonWebConfig {

    @Bean
    @ConditionalOnMissingBean
    public MappingJackson2HttpMessageConverter jacksonMessageConverter(ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(CommonConfig::getObjectMapper);
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}
