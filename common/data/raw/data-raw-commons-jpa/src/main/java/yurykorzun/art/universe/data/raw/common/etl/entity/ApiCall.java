package yurykorzun.art.universe.data.raw.common.etl.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.converter.MapConverter;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * <p>Base class for all api calls. </p>
 * <p>Child classes must provide their own ID field.</p>
 */
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public abstract class ApiCall extends BaseEntity {

    public abstract ApiCallType getType();

    @Column(name = "parameters")
    @Convert(converter = MapConverter.class)
    @Builder.Default
    private Map<String, String> params = Collections.emptyMap();

    @Column(name = "status")
    @Convert(converter = ApiCallStatusConverter.class)
    @Builder.Default
    private ApiCallStatus status = ApiCallStatus.CREATED;

    @NonNull
    @Column(name = "due_dttm")
    private Instant dueDttm;

    protected void validateParams(ApiCallType type, Map<String, String> params) {
        // TODO check optional/default params overriding and add tests
        Collection<String> paramNames = type.getMandatoryParams();
        if (paramNames.isEmpty()) return;
        params.forEach((k, v) -> {
            if (!paramNames.contains(k)) {
                throw new IllegalArgumentException(
                        String.format("Unknown parameter %s for an api call %s", k, type.getMethod()));
            }
        });
    }

    public void setStatus(ApiCallStatus newStatus) {
        if (!this.status.isValidTransition(newStatus)) {
            throw new IllegalArgumentException(String.format("Invalid transition from %s to %s", this.status, newStatus));
        }
        this.status = newStatus;
    }
}
