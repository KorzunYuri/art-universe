package yurykorzun.art.universe.music.data.master.entity;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.CodedRegistry;

import static org.junit.jupiter.api.Assertions.*;

class MasterApprovalStatusTest {

    @Test
    void getCode_shouldReturnCorrectCodes() {
        assertEquals(0, MasterApprovalStatus.NEW.getCode());
        assertEquals(1, MasterApprovalStatus.PENDING.getCode());
        assertEquals(2, MasterApprovalStatus.APPROVED.getCode());
        assertEquals(3, MasterApprovalStatus.REJECTED.getCode());
    }

    @Test
    void getName_shouldReturnCorrectNames() {
        assertEquals("new", MasterApprovalStatus.NEW.getName());
        assertEquals("pending", MasterApprovalStatus.PENDING.getName());
        assertEquals("approved", MasterApprovalStatus.APPROVED.getName());
        assertEquals("rejected", MasterApprovalStatus.REJECTED.getName());
    }

    @Test
    void codedRegistry_shouldContainAllValues() {
        for (MasterApprovalStatus status : MasterApprovalStatus.values()) {
            var found = CodedRegistry.getByCode(status.getCode(), MasterApprovalStatus.class);
            assertTrue(found.isPresent(), "MasterApprovalStatus " + status + " should be in CodedRegistry");
            assertEquals(status, found.get());
        }
    }

    @Test
    void codedRegistry_shouldReturnEmptyForUnknownCode() {
        var found = CodedRegistry.getByCode(999, MasterApprovalStatus.class);
        assertTrue(found.isEmpty());
    }

    @Test
    void fromString_shouldResolveByName() {
        assertEquals(MasterApprovalStatus.NEW, MasterApprovalStatus.fromString("new"));
        assertEquals(MasterApprovalStatus.PENDING, MasterApprovalStatus.fromString("pending"));
        assertEquals(MasterApprovalStatus.APPROVED, MasterApprovalStatus.fromString("approved"));
        assertEquals(MasterApprovalStatus.REJECTED, MasterApprovalStatus.fromString("rejected"));
    }

    @Test
    void fromString_shouldBeCaseInsensitive() {
        assertEquals(MasterApprovalStatus.APPROVED, MasterApprovalStatus.fromString("APPROVED"));
        assertEquals(MasterApprovalStatus.PENDING, MasterApprovalStatus.fromString("Pending"));
    }

    @Test
    void fromString_withInvalidName_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> MasterApprovalStatus.fromString("invalid"));
    }

    @Test
    void values_shouldContainExactlyFourEntries() {
        assertEquals(4, MasterApprovalStatus.values().length);
    }
}
