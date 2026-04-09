package yurykorzun.art.universe.music.data.master.entity.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum AttributeTemporalType implements Coded {
    CONSTANT(1, "constant"),
    INSTANT(2, "instant"),
    PERIOD(3, "period");

    private final int code;
    private final String name;

    AttributeTemporalType(int code, String name) {
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
    public static AttributeTemporalType fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Attribute temporal type name cannot be null");
        }
        for (AttributeTemporalType type : AttributeTemporalType.values()) {
            if (type.name.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown attribute temporal type: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), AttributeTemporalType.class);
    }
}
