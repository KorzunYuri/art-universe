package yurykorzun.art.universe.music.data.semantic.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum AnalysisMode implements Coded {
    FULL_EXTRACTION(1, "full_extraction"),
    CREATIVE_CATEGORIZATION(2, "creative_categorization");

    private final int code;
    private final String name;

    AnalysisMode(int code, String name) {
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
    public static AnalysisMode fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Analysis mode name cannot be null");
        }
        for (AnalysisMode mode : AnalysisMode.values()) {
            if (mode.name.equalsIgnoreCase(name) || mode.name().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown analysis mode: " + name);
    }

    public static AnalysisMode fromCode(int code) {
        for (AnalysisMode mode : AnalysisMode.values()) {
            if (mode.code == code) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown analysis mode code: " + code);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), AnalysisMode.class);
    }
}
