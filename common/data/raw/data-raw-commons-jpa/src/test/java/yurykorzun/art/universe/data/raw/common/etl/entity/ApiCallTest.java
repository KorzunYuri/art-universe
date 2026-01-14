package yurykorzun.art.universe.data.raw.common.etl.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ApiCallTest {

    private static final Supplier<ApiCall> validApiCallSupplier = () -> TestApiCall.builder().dueDttm(Instant.now()).build();


    @Test
    void testValidApiCallInitialState() {
        ApiCall call = validApiCallSupplier.get();
        assertEquals(ApiCallStatus.CREATED, call.getStatus());
        assertTrue(call.getParams().isEmpty());
    }

    @Test
    void testValidApiCallStatusTransition() {
        ApiCall call = validApiCallSupplier.get();
        call.setStatus(ApiCallStatus.PENDING);
        assertEquals(ApiCallStatus.PENDING, call.getStatus());
    }

    @Test
    void testInvalidApiCallStatusTransition() {
        ApiCall call = validApiCallSupplier.get();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> call.setStatus(ApiCallStatus.CREATED));
        assertEquals("Invalid transition from CREATED to CREATED", e.getMessage());
    }

    @Test
    void testIncompleteApiCallException() {
        List<Supplier<ApiCall>> incompleteApiCallSuppliers = List.of(
                () -> TestApiCall.builder().build()
        );
        incompleteApiCallSuppliers.forEach(s -> {
            Exception e = assertThrows(Exception.class, s::get);
            assertInstanceOf(NullPointerException.class, e);
            assertTrue(e.getMessage().endsWith(" is marked non-null but is null"));
        });
    }
}