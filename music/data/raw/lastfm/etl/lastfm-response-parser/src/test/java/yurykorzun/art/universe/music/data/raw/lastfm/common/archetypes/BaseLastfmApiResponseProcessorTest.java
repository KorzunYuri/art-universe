package yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes;

import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.api.config.LastfmThresholdConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.BlacklistedEntityUrlService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute.LastfmAttributeHistoryProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.config.TaskCoordinationTestAutoConfiguration;

/**
 * Base test class for LastFM API response processor tests.
 * Contains common imports shared across all processor tests.
 */
@Import({
        // Processing infrastructure
        LastfmApiDtoProcessingService.class,
        // Quality control
        BlacklistedEntityUrlService.class,
        DtoQualityService.class,
        LastfmThresholdConfig.class,
        // Attributes infrastructure
        LastfmAttributeHistoryServiceImpl.class,
        LastfmAttributeTypeSynchronizer.class,
        LastfmAttributeHistoryProcessor.class,
        // other mandatory beans & configs
        TaskCoordinationTestAutoConfiguration.class,
})
public abstract class BaseLastfmApiResponseProcessorTest extends LastfmJpaTestHelper {
}
