package yurykorzun.art.universe.common.testing;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO used to verify project-wide Jackson serialization rules.
 * <p>
 * The two rules under test:
 * <ol>
 *   <li>Boolean getters with an {@code is} prefix are <em>not</em> renamed — the property
 *       serializes as {@code isSomething}, not {@code something}.</li>
 *   <li>Date/time fields ({@link Instant}, {@link LocalDateTime}) serialize as ISO-8601
 *       strings, not as numeric timestamps.</li>
 * </ol>
 * Provided in main sources so that consumer modules can reference it in their own tests
 * without any extra test-fixture configuration.
 */
public class SerializationTestDto {

    private boolean something;
    private Instant createdAt;
    private LocalDateTime updatedAt;

    public SerializationTestDto() {
    }

    public SerializationTestDto(boolean something, Instant createdAt, LocalDateTime updatedAt) {
        this.something = something;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Boolean getter intentionally prefixed with {@code is} to exercise naming rules. */
    public boolean isSomething() {
        return something;
    }

    public void setSomething(boolean something) {
        this.something = something;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
