package yurykorzun.art.universe.common.web.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yurykorzun.art.universe.common.testing.SerializationTestDto;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Minimal REST controller used by {@link WebSerializationTest} to verify Jackson serialization rules.
 * Lives in testFixtures so it is available to consumer modules without being included in the production jar.
 */
@RestController
@RequestMapping("/commons/test-serialization")
public class WebSerializationTestController {

    @GetMapping
    public SerializationTestDto getTestDto() {
        return new SerializationTestDto(
                true,
                Instant.parse("2024-01-15T10:30:00Z"),
                LocalDateTime.of(2024, 1, 15, 10, 30, 0)
        );
    }
}
