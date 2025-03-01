package yurykorzun.art.universe.common.persistence.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.converters.DataCollectionTaskTypeConverter;

import javax.persistence.*;
import java.time.Instant;

@MappedSuperclass
@SuperBuilder
@Getter
@NoArgsConstructor
public class DataCollectionTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NonNull
    @Column(name = "task_type")
    @Convert(converter = DataCollectionTaskTypeConverter.class)
    private DataCollectionTaskType type;

    @NonNull
    @Column(name = "due_dttm")
    private Instant dueDttm;

    @NonNull
    @Builder.Default
    @Column(name = "status")
    private TaskStatus status = TaskStatus.CREATED;

    @Builder.Default
    @Column(name = "attempt_cnt")
    private int attemptCnt = 0;

    public void setStatus(TaskStatus newStatus) {
        if (this.status.isValidTransition(newStatus)) {
            throw new IllegalStateException(String.format("Invalid transition from %s to %s", this.status, newStatus));
        }
        this.status = newStatus;
    }

    public void incAttempts() {
        attemptCnt++;
    }
}
