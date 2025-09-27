package yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import yurykorzun.art.universe.common.exception.CommonGlobalExceptionHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.common.exception.GlobalExceptionHandler;

/**
 * Base class for MVC tests that includes global exception handlers.
 * This ensures that both common and module-specific exception handlers are available in tests.
 */
@ActiveProfiles("test")
@Import({
    CommonGlobalExceptionHandler.class,
    GlobalExceptionHandler.class
})
public abstract class BaseMvcTest {
}
