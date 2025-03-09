package yurykorzun.art.universe.common.persistence.entity;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public class BaseEntity {

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", updatable = true)
    private LocalDateTime updatedAt = LocalDateTime.now();

}
