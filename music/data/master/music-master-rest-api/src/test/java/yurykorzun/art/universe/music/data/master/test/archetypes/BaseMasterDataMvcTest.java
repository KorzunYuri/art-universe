package yurykorzun.art.universe.music.data.master.test.archetypes;

import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.master.config.WebMvcTestConfig;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;

@Import({
    WebMvcTestConfig.class,
})
public class BaseMasterDataMvcTest extends BaseMvcTest {
}
