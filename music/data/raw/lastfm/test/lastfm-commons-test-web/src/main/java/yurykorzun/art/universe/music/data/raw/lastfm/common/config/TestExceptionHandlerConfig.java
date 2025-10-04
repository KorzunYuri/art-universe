package yurykorzun.art.universe.music.data.raw.lastfm.common.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.exception.CommonGlobalExceptionHandler;

/**
 * Test configuration that imports all necessary exception handlers for MVC tests.
 * This ensures that both common and module-specific exception handlers are available.
 */
@TestConfiguration
@Import({
    CommonGlobalExceptionHandler.class
})
public class TestExceptionHandlerConfig {
}
