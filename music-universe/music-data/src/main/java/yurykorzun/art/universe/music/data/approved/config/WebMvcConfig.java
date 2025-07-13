package yurykorzun.art.universe.music.data.approved.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.entity.EntityType;

/**
 * Configuration for WebMvc to handle enum conversion
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Register converter for EntityType
        registry.addConverter(String.class, EntityType.class, EntityType::fromString);
        
        // Register converter for DataSource
        registry.addConverter(String.class, DataSource.class, DataSource::fromString);
    }
}
