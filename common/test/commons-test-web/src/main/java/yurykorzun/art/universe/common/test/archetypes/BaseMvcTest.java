package yurykorzun.art.universe.common.test.archetypes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.web.exception.CommonGlobalExceptionHandler;

/**
 * Base class for MVC tests that includes common exception handlers.
 * Concrete subclasses should be annotated with {@code @WebMvcTest}.
 * <p>
 * Spring Security auto-configuration is excluded because MVC unit tests validate
 * controller logic, not authentication. Without this exclusion, modules that depend
 * on {@code commons-security} would get Spring Security's default Basic auth applied
 * even when the project's JWT security is inactive (no {@code auth.jwt.secret}).
 */
@ImportAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class
})
@Import({
    CommonGlobalExceptionHandler.class
})
public abstract class BaseMvcTest extends BaseTest {

    @Autowired
    protected MockMvc mockMvc;
}
