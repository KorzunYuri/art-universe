package yurykorzun.art.universe.common.persistence.entity;

import lombok.Getter;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Getter
public enum RequestStatus {
    CREATED(1),
    PROCESSING(2),
    SUCCESS(3),
    ERROR(4);

    private final int id;

    RequestStatus(int id) {
        this.id = id;
    }

    // Mapping for convenient retrieval of RequestStatus by id
    private static final Map<Integer, RequestStatus> idMap = new HashMap<>();
    static {
        for (RequestStatus status : RequestStatus.values()) {
            if (idMap.putIfAbsent(status.id, status) != null) {
                throw new IllegalArgumentException("Duplicate RequestStatus %d".formatted(status.id));
            }
        }
    }

    public static RequestStatus getById(int id) {
        return idMap.get(id);
    }

    //  status transition validation

    public boolean isValidTransition(RequestStatus to) {
        return Transition.transitions.getOrDefault(this, Map.of()).containsKey(to);
    }

    private enum Transition {
        START_PROCESSING(CREATED, PROCESSING),
        FINISHED_SUCCESS(PROCESSING, SUCCESS),
        FINISHED_PROCESSING(PROCESSING, ERROR);

        private final RequestStatus from;
        private final RequestStatus to;

        private static final Map<RequestStatus, Map<RequestStatus, Transition>> transitions =
                Stream.of(Transition.values())
                        .collect(groupingBy(
                                t -> t.from,
                                () -> new EnumMap<>(RequestStatus.class),
                                toMap(
                                        t -> t.to,
                                        t -> t,
                                        (x, y) -> y, () -> new EnumMap<>(RequestStatus.class))));

        Transition(RequestStatus from, RequestStatus to) {
            this.from = from;
            this.to = to;
        }
    }
}