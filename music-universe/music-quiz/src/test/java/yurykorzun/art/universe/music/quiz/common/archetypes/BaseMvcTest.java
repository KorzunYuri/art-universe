package yurykorzun.art.universe.music.quiz.common.archetypes;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import yurykorzun.art.universe.common.exception.CommonGlobalExceptionHandler;

/**
 * Base class for MVC tests that includes global exception handlers.
 * This ensures that the common exception handler is available in tests.
 */
@ActiveProfiles("test")
@Import({
    CommonGlobalExceptionHandler.class
})
public abstract class BaseMvcTest {
}
