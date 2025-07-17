package yurykorzun.art.universe.music.quiz.common.archetypes;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import yurykorzun.art.universe.music.quiz.common.DynamicPropertySourceInitializer;

@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(initializers = DynamicPropertySourceInitializer.class)
public abstract class BaseTest {
}
