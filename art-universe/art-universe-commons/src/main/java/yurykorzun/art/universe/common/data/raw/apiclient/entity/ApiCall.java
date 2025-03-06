package yurykorzun.art.universe.common.data.raw.apiclient.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.converter.MapConverter;

import javax.persistence.*;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public class ApiCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "type")
    @Convert(converter = ApiCallTypeConverter.class)
    private ApiCallType type;

    @Column(name = "parameters")
    @Convert(converter = MapConverter.class)
    @Builder.Default
    private Map<String, String> params = Collections.emptyMap();

    @Column(name = "status")
    @Builder.Default
    private ApiCallStatus status = ApiCallStatus.CREATED;

    @NonNull
    @Column(name = "due_dttm")
    private Instant dueDttm;

    public ApiCall(ApiCallType type, Map<String, String> params) {

        validateParams(type, params);

        this.type = type;
        this.params = params;
    }

    protected void validateParams(ApiCallType type, Map<String, String> params) {
        // TODO check optional/default params overriding
        Collection<String> paramNames = type.getMandatoryParams();
        if (paramNames.isEmpty()) return;
        params.forEach((k, v) -> {
            if (!paramNames.contains(k)) {
                throw new IllegalArgumentException(
                        String.format("Unknown parameter %s for an api call %s", k, type.getMethod()));
            }
        });
    }

    public void setStatus(ApiCallStatus status) {
        // TODO validate status transitions (watch Task.class for example)
        this.status = status;
    }
}
