package yurykorzun.art.universe.common.data.raw.task.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

import jakarta.persistence.*;
import java.time.Instant;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NonNull
    @Column(name = "task_type")
    @Convert(converter = TaskTypeConverter.class)
    private TaskType type;

    @NonNull
    @Column(name = "due_dttm")
    private Instant dueDttm;

    @NonNull
    @Builder.Default
    @Column(name = "status")
    @Convert(converter = TaskStatusConverter.class)
    private TaskStatus status = TaskStatus.CREATED;

    @Builder.Default
    @Column(name = "attempt_cnt")
    private int attemptCnt = 0;

    public void setStatus(TaskStatus newStatus) {
        if (!this.status.isValidTransition(newStatus)) {
            throw new IllegalArgumentException(String.format("Invalid transition from %s to %s", this.status, newStatus));
        }
        this.status = newStatus;
    }

    public void incAttempts() {
        attemptCnt++;
    }
}
