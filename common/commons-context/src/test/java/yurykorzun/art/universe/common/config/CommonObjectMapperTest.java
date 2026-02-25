package yurykorzun.art.universe.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import yurykorzun.art.universe.common.testing.SerializationTestDto;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the {@link ObjectMapper} bean registered by {@link CommonConfig}
 * follows the project serialization rules:
 * <ul>
 *   <li>Boolean getters with {@code is} prefix are NOT renamed (isSomething → "isSomething").</li>
 *   <li>Date/time types are serialized as ISO-8601 strings, not timestamps.</li>
 * </ul>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CommonConfig.class)
class CommonObjectMapperTest {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void objectMapper_booleanGetterWithIsPrefix_isNotRenamed() throws JsonProcessingException {
        var dto = new SerializationTestDto(true, null, null);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"isSomething\"");
        assertThat(json).doesNotContain("\"something\":");
    }

    @Test
    void objectMapper_instantField_serializedAsISO8601String() throws JsonProcessingException {
        var dto = new SerializationTestDto(false, Instant.parse("2024-01-15T10:30:00Z"), null);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"2024-01-15T10:30:00Z\"");
    }

    @Test
    void objectMapper_localDateTimeField_serializedAsISO8601String() throws JsonProcessingException {
        var dto = new SerializationTestDto(false, null, LocalDateTime.of(2024, 1, 15, 10, 30, 0));

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"2024-01-15T10:30:00\"");
    }
}
