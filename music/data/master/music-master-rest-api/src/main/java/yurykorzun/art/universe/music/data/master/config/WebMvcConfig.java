package yurykorzun.art.universe.music.data.master.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;

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
        registry.addConverter(String.class, EntityType.class, name -> MasterEntityType.fromString(name));

        // Register converter for DataSource
        registry.addConverter(String.class, DataSource.class, DataSource::fromString);

        // Register converters for origin/approval-status (used by internal ETL endpoints)
        registry.addConverter(String.class, Origin.class, Origin::fromString);
        registry.addConverter(String.class, MasterApprovalStatus.class, MasterApprovalStatus::fromString);
    }
}
