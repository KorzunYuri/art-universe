package yurykorzun.art.universe.music.data.master.model.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum AttributeComputationType implements Coded {
    RAW(1, "raw"),
    DERIVED(2, "derived"),
    COMPOSITE(3, "composite"),
    SEMANTIC(4, "semantic"),
    MANUAL(5, "manual");

    private final int code;
    private final String name;

    AttributeComputationType(int code, String name) {
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
    public static AttributeComputationType fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Attribute computation type name cannot be null");
        }
        for (AttributeComputationType type : AttributeComputationType.values()) {
            if (type.name.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown attribute computation type: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), AttributeComputationType.class);
    }
}
