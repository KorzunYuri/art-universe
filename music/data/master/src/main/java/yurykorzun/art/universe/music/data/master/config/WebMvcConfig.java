package yurykorzun.art.universe.music.data.master.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import yurykorzun.art.universe.common.web.config.CommonWebConfig;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

/**
 * Configuration for WebMvc to handle enum conversion
 */
@Configuration
@Import(
    CommonWebConfig.class
)
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Register converter for EntityType
        registry.addConverter(String.class, MasterEntityType.class, MasterEntityType::fromString);
        
        // Register converter for DataSource
        registry.addConverter(String.class, DataSource.class, DataSource::fromString);
    }
}
