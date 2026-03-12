package yurykorzun.art.universe.music.data.master.entity;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.CodedRegistry;

import static org.junit.jupiter.api.Assertions.*;

class OriginTest {

    @Test
    void getCode_shouldReturnCorrectCodes() {
        assertEquals(0, Origin.UNKNOWN.getCode());
        assertEquals(1, Origin.MANUAL.getCode());
        assertEquals(2, Origin.ETL.getCode());
        assertEquals(3, Origin.AI.getCode());
    }

    @Test
    void getName_shouldReturnCorrectNames() {
        assertEquals("unknown", Origin.UNKNOWN.getName());
        assertEquals("manual", Origin.MANUAL.getName());
        assertEquals("etl", Origin.ETL.getName());
        assertEquals("ai", Origin.AI.getName());
    }

    @Test
    void codedRegistry_shouldContainAllValues() {
        for (Origin origin : Origin.values()) {
            var found = CodedRegistry.getByCode(origin.getCode(), Origin.class);
            assertTrue(found.isPresent(), "Origin " + origin + " should be in CodedRegistry");
            assertEquals(origin, found.get());
        }
    }

    @Test
    void codedRegistry_shouldReturnEmptyForUnknownCode() {
        var found = CodedRegistry.getByCode(999, Origin.class);
        assertTrue(found.isEmpty());
    }

    @Test
    void fromString_shouldResolveByName() {
        assertEquals(Origin.UNKNOWN, Origin.fromString("unknown"));
        assertEquals(Origin.MANUAL, Origin.fromString("manual"));
        assertEquals(Origin.ETL, Origin.fromString("etl"));
        assertEquals(Origin.AI, Origin.fromString("ai"));
    }

    @Test
    void fromString_shouldBeCaseInsensitive() {
        assertEquals(Origin.UNKNOWN, Origin.fromString("UNKNOWN"));
        assertEquals(Origin.MANUAL, Origin.fromString("MANUAL"));
        assertEquals(Origin.ETL, Origin.fromString("Etl"));
        assertEquals(Origin.AI, Origin.fromString("AI"));
    }

    @Test
    void fromString_withInvalidName_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> Origin.fromString("invalid"));
    }

    @Test
    void values_shouldContainExactlyThreeEntries() {
        assertEquals(4, Origin.values().length);
    }
}
