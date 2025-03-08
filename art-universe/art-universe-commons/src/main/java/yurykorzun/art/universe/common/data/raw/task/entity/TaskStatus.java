package yurykorzun.art.universe.common.data.raw.task.entity;

import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.TransitionAware;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;


public enum TaskStatus implements Coded, TransitionAware<TaskStatus> {
    CREATED(1),
    PROCESSING(2),
    SUCCESS(3),
    ERROR(4),
    RETRY(5),
    SKIPPED(6);

    private final int code;

    TaskStatus(int code) {
        this.code = code;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), TaskStatus.class);
    }

    @Override
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