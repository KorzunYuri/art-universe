package yurykorzun.art.universe.common.pgnotify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PgChannelValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"channel", "lastfm_calls_ready", "a1", "abc123_def"})
    void isValid_shouldReturnTrue_forValidChannelNames(String channel) {
        assertTrue(PgChannelValidator.isValid(channel));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "UPPER", "123start", "_leading", "has-dash", "has space", "semi;colon", "'; DROP TABLE --"})
    void isValid_shouldReturnFalse_forInvalidChannelNames(String channel) {
        assertFalse(PgChannelValidator.isValid(channel));
    }

    @Test
    void isValid_shouldReturnFalse_forNull() {
        assertFalse(PgChannelValidator.isValid(null));
    }

    @Test
    void requireValid_shouldNotThrow_forValidChannel() {
        assertDoesNotThrow(() -> PgChannelValidator.requireValid("valid_channel"));
    }

    @Test
    void requireValid_shouldThrow_forInvalidChannel() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> PgChannelValidator.requireValid("BAD;channel")
        );
        assertTrue(ex.getMessage().contains("BAD;channel"));
    }
}
