package yurykorzun.art.universe.common.test.archetypes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.web.exception.CommonGlobalExceptionHandler;

/**
 * Base class for MVC tests that includes common exception handlers.
 * Concrete subclasses should be annotated with {@code @WebMvcTest}.
 * <p>
 * Security filters are disabled ({@code addFilters = false}) because MVC unit tests
 * validate controller logic, not authentication. Modules that depend on
 * {@code commons-security} would otherwise get Spring Security's default Basic auth
 * applied even when the project's JWT security is inactive.
 */
@AutoConfigureMockMvc(addFilters = false)
@Import({
    CommonGlobalExceptionHandler.class
})
public abstract class BaseMvcTest extends BaseTest {

    @Autowired
    protected MockMvc mockMvc;
}
