package yurykorzun.art.universe.common.persistence.entity;

import lombok.Getter;

import javax.persistence.*;
import java.time.Instant;

@MappedSuperclass
@Getter
public class DataCollectionRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "from_dttm")
    private Instant fromDttm;

    @Column(name = "to_dttm")
    private Instant toDttm;

    private RequestStatus status = RequestStatus.CREATED;

    public DataCollectionRequest() {
    }

    public DataCollectionRequest(Instant fromDttm, Instant toDttm) {
        this.fromDttm = fromDttm;
        this.toDttm = toDttm;
    }

    public void setStatus(RequestStatus newStatus) {
        if (this.status.isValidTransition(newStatus)) {
            throw new IllegalStateException(String.format("Invalid transition from %s to %s", this.status, newStatus));
        }
        this.status = newStatus;
    }
}
