package yurykorzun.art.universe.music.data.master.exception;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.exception.ExposedException;

import static org.junit.jupiter.api.Assertions.*;

class DiamondRelationExceptionTest {

    @Test
    void shouldCreateException_withMessage() {
        DiamondRelationException exception = new DiamondRelationException("diamond detected");

        assertEquals("diamond detected", exception.getMessage());
        assertInstanceOf(ExposedException.class, exception);
    }
}
