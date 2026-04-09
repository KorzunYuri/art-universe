package yurykorzun.art.universe.music.data.master.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum Origin implements Coded {
    UNKNOWN(0, "unknown"),
    MANUAL(1, "manual"),
    ETL(2, "etl"),
    AI(3, "ai");

    private final int code;
    private final String name;

    Origin(int code, String name) {
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
    public static Origin fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Origin name cannot be null");
        }
        for (Origin origin : Origin.values()) {
            if (origin.name.equalsIgnoreCase(name) || origin.name().equalsIgnoreCase(name)) {
                return origin;
            }
        }
        throw new IllegalArgumentException("Unknown origin: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), Origin.class);
    }
}
