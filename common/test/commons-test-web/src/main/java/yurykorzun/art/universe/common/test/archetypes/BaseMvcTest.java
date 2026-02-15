package yurykorzun.art.universe.common.test.archetypes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.web.exception.CommonGlobalExceptionHandler;

/**
 * Base class for MVC tests that includes common exception handlers.
 * Concrete subclasses should be annotated with {@code @WebMvcTest}.
 */
@Import({
    CommonGlobalExceptionHandler.class
})
public abstract class BaseMvcTest extends BaseTest {

    @Autowired
    protected MockMvc mockMvc;
}
