package yurykorzun.art.universe.common.data.raw.task.entity;

import lombok.Getter;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Getter
public enum TaskStatus {
    CREATED(1),
    PROCESSING(2),
    SUCCESS(3),
    ERROR(4),
    RETRY(5),
    SKIPPED(6);

    private final int id;

    TaskStatus(int id) {
        this.id = id;
    }

    // Mapping for convenient retrieval of RequestStatus by id
    private static final Map<Integer, TaskStatus> idMap = new HashMap<>();
    static {
        for (TaskStatus status : TaskStatus.values()) {
            if (idMap.putIfAbsent(status.id, status) != null) {
                throw new IllegalArgumentException("Duplicate TaskStatus %d".formatted(status.id));
            }
        }
    }

    public static TaskStatus getById(int id) {
        return idMap.get(id);
    }

    //  status transition validation

    public boolean isValidTransition(TaskStatus to) {
        return Transition.transitions.getOrDefault(this, Map.of()).containsKey(to);
    }

    private enum Transition {
        STARTED(CREATED, PROCESSING),
        OUTDATED(CREATED, SKIPPED),
        DUE_TO_RETRY(PROCESSING, RETRY),
        RETRIED(RETRY, PROCESSING),
        SUCCESSFUL(PROCESSING, SUCCESS),
        FAILED(PROCESSING, ERROR);

        private final TaskStatus from;
        private final TaskStatus to;

        private static final Map<TaskStatus, Map<TaskStatus, Transition>> transitions =
                Stream.of(Transition.values())
                        .collect(groupingBy(
                                t -> t.from,
                                () -> new EnumMap<>(TaskStatus.class),
                                toMap(
                                        t -> t.to,
                                        t -> t,
                                        (x, y) -> y, () -> new EnumMap<>(TaskStatus.class))));

        Transition(TaskStatus from, TaskStatus to) {
            this.from = from;
            this.to = to;
        }
    }
}