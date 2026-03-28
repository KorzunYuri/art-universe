package yurykorzun.art.universe.music.data.semantic.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum AnalysisRequestStatus implements Coded {
    PENDING(1, "pending"),
    PROCESSING(2, "processing"),
    COMPLETED(3, "completed"),
    FAILED(4, "failed");

    private final int code;
    private final String name;

    AnalysisRequestStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    @JsonValue
    public String getName() {
        return name;
    }

    @JsonCreator
    public static AnalysisRequestStatus fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Analysis request status name cannot be null");
        }
        for (AnalysisRequestStatus status : AnalysisRequestStatus.values()) {
            if (status.name.equalsIgnoreCase(name) || status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown analysis request status: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), AnalysisRequestStatus.class);
    }
}
