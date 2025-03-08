package yurykorzun.art.universe.common.data.raw.apiclient.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.TransitionAware;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Getter
public enum ApiCallStatus implements Coded, TransitionAware<ApiCallStatus> {

    CREATED(1),
    SCHEDULED(2),
    EXPIRED(3),
    CANCELLED(4),
    PROCESSING(5),
    SUCCESSFUL(6),
    DUE_TO_RETRY(7),
    FAILED(8);

    private final int code;

    ApiCallStatus(int code) {
        this.code = code;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), ApiCallStatus.class);
    }

    @Override
    public boolean isValidTransition(ApiCallStatus to) {
        return ApiCallStatus.Transition.transitions.getOrDefault(this, Map.of()).containsKey(to);
    }

    private enum Transition {
        CREATED_TO_SCHEDULED    (CREATED,       SCHEDULED),
        CREATED_TO_EXPIRED      (CREATED,       EXPIRED),
        CREATED_TO_CANCELLED    (CREATED,       CANCELLED),
        SCHEDULED_TO_EXPIRED    (SCHEDULED,     EXPIRED),
        SCHEDULED_TO_PROCESSING (SCHEDULED,     PROCESSING),
        PROCESSING_TO_SUCCESSFUL(PROCESSING,    SUCCESSFUL),
        PROCESSING_TO_RETRY     (PROCESSING,    DUE_TO_RETRY),
        RETRY_TO_PROCESSING     (DUE_TO_RETRY,  PROCESSING),
        RETRY_TO_SCHEDULED      (DUE_TO_RETRY,  SCHEDULED),
        PROCESSING_TO_FAILED    (PROCESSING,    FAILED);

        private final ApiCallStatus from;
        private final ApiCallStatus to;

        private static final Map<ApiCallStatus, Map<ApiCallStatus, ApiCallStatus.Transition>> transitions =
                Stream.of(ApiCallStatus.Transition.values())
                        .collect(groupingBy(
                                t -> t.from,
                                () -> new EnumMap<>(ApiCallStatus.class),
                                toMap(
                                        t -> t.to,
                                        t -> t,
                                        (x, y) -> y, () -> new EnumMap<>(ApiCallStatus.class))));

        Transition(ApiCallStatus from, ApiCallStatus to) {
            this.from = from;
            this.to = to;
        }
    }

}
