package yurykorzun.art.universe.common.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal {@code @SpringBootApplication} required by {@code @WebMvcTest} in
 * {@link yurykorzun.art.universe.common.web.config.WebSerializationTest} to locate a
 * {@code @SpringBootConfiguration}.
 * <p>
 * Spring Boot's test context bootstrapper searches upward through parent packages from the test
 * class. {@code WebSerializationTest} lives in {@code ...common.web.config}; one level up is
 * {@code ...common.web}, where this class is found.
 * <p>
 * Lives in testFixtures so it is available to consumer modules without ending up in the
 * production jar.
 */
@SpringBootApplication
public class WebMvcTestApplication {
}
