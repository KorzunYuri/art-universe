package yurykorzun.art.universe.music.data.master.common.archetypes;

import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.master.config.WebMvcTestConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.BaseMvcTest;

@Import({
    WebMvcTestConfig.class,
})
public class BaseMasterDataMvcTest extends BaseMvcTest {
}
