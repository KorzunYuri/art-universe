package yurykorzun.art.universe.common.test.archetypes;

import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.web.exception.CommonGlobalExceptionHandler;

/**
 * Base class for MVC tests that includes global exception handlers.
 * This ensures that both common and module-specific exception handlers are available in tests.
 */
@Import({
    CommonGlobalExceptionHandler.class
})
public abstract class BaseMvcTest extends BaseTest {
}
