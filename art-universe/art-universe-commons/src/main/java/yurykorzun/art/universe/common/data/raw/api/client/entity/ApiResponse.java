package yurykorzun.art.universe.common.data.raw.api.client.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

import java.util.Objects;

/**
 * Basic response of datasource API. Child classes must have 'response_body' field and take care of its persistence
 */
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public abstract class ApiResponse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NonNull
    @Column(name = "api_call_id")
    private Long apiCallId;

    @NonNull
    @Column(name = "api_call_type")
    @Convert(converter = ApiCallTypeConverter.class)
    private ApiCallType apiCallType;

    @Column(name = "status")
    @Convert(converter = ApiResponseStatusConverter.class)
    @Builder.Default
    private ApiResponseStatus status = ApiResponseStatus.PENDING;

    public void setStatus(ApiResponseStatus newStatus) {
        if (!this.status.isValidTransition(newStatus)) {
            throw new IllegalArgumentException(String.format("Invalid transition from %s to %s", this.status, newStatus));
        }
        this.status = newStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ApiResponse other = (ApiResponse) o;
        if (Objects.equals(apiCallType, other.apiCallType)) {
            if (this.getId() != 0 && other.getId() != 0) {
                return this.getId() == other.getId();
            } else {
                return Objects.equals(apiCallId, other.apiCallId);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.getId() != 0) {
            return Objects.hash(id, apiCallType);
        } else {
            return Objects.hash(apiCallId, apiCallType);
        }
    }
}
