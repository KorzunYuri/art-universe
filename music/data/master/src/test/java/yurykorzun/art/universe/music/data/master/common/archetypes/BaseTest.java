package yurykorzun.art.universe.music.data.master.common.archetypes;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import yurykorzun.art.universe.common.config.CommonConfig;
import yurykorzun.art.universe.music.data.master.common.DynamicPropertySourceInitializer;

@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(initializers = DynamicPropertySourceInitializer.class)
@Import({
    CommonConfig.class
})
public abstract class BaseTest {
}
