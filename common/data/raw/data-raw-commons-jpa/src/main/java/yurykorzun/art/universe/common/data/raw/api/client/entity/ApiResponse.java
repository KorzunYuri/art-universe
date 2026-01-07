package yurykorzun.art.universe.common.data.raw.api.client.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

/**
 * <p>Base class for responses from data source API.</p>
 * Child classes must:
 * <ul>
 *     <li>have their own ID field</li>
 *     <li>have 'response_body' field and take care of its persistence</li>
 * </ul>
 */
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public abstract class ApiResponse extends BaseEntity {

    public abstract ApiCall getApiCall();
    public abstract String getResponseBody();

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
}
