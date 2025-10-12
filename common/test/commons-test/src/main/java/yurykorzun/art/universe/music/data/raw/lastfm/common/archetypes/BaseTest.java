package yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import yurykorzun.art.universe.common.config.CommonConfig;

@ActiveProfiles("test")
@Import({
    CommonConfig.class
})
public abstract class BaseTest {
}
