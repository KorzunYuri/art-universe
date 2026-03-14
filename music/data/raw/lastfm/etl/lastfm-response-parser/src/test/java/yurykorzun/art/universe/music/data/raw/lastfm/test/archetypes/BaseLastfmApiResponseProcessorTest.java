package yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes;

import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.config.MappingConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingOrchestrator;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.BlacklistedEntityUrlService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.processor.LastfmAttributeHistoryProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl.LastfmAttributeHistoryStagingServiceImpl;

/**
 * Base test class for LastFM API response processor tests.
 * Contains common imports shared across all processor tests.
 */
@Import({
        // Processing infrastructure
        LastfmApiDtoProcessingOrchestrator.class,
        // Quality control
        BlacklistedEntityUrlService.class,
        DtoQualityService.class,
        // Attributes infrastructure
        LastfmAttributeHistoryStagingServiceImpl.class,
        LastfmAttributeHistoryProcessor.class,
        // other mandatory beans & configs
        MappingConfig.class
})
public abstract class BaseLastfmApiResponseProcessorTest extends LastfmJpaTestHelper {
}
