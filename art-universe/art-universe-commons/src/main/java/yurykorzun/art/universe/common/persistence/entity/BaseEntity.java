package yurykorzun.art.universe.common.persistence.entity;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Setter
public class BaseEntity {

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", updatable = true)
    private Instant updatedAt = Instant.now();

}
