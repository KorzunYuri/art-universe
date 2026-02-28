package yurykorzun.art.universe.music.data.master.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import yurykorzun.art.universe.art.data.models.entity.ArtEntityType;
import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

/**
 * Configuration for WebMvc to handle enum conversion
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Register converter for MasterEntityType (used by binding endpoints)
        registry.addConverter(String.class, MasterEntityType.class, MasterEntityType::fromString);

        // Register converter for EntityType (used by internal relation endpoints)
        // Tries MasterEntityType first, then ArtEntityType
        registry.addConverter(String.class, EntityType.class, name -> {
            try {
                return MasterEntityType.fromString(name);
            } catch (IllegalArgumentException e) {
                return ArtEntityType.fromString(name);
            }
        });

        // Register converter for DataSource
        registry.addConverter(String.class, DataSource.class, DataSource::fromString);
    }
}
