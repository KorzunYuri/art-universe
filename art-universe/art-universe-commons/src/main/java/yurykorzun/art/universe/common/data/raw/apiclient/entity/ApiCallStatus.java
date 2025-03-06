package yurykorzun.art.universe.common.data.raw.apiclient.entity;

import lombok.Getter;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Getter
public enum ApiCallStatus {
    CREATED(1),
    SCHEDULED(2),
    EXPIRED(3),
    CANCELLED(4),
    PROCESSING(5),
    SUCCESSFUL(6),
    DUE_TO_RETRY(7),
    FAILED(8);

    private final int id;

    ApiCallStatus(int id) {
        this.id = id;
    }

    // Mapping for convenient retrieval of RequestStatus by id
    private static final Map<Integer, ApiCallStatus> idMap = new HashMap<>();
    static {
        for (ApiCallStatus status : ApiCallStatus.values()) {
            if (idMap.putIfAbsent(status.id, status) != null) {
                throw new IllegalArgumentException("Duplicate ApiCallStatus %d".formatted(status.id));
            }
        }
    }

    public static ApiCallStatus getById(int id) {
        return idMap.get(id);
    }

    //  status transition validation

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
