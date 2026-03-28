package yurykorzun.art.universe.music.data.master.entity.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum AttributeDataType implements Coded {
    NUMERIC(1, "numeric"),
    STRING(2, "string"),
    DATE(3, "date"),
    BOOLEAN(4, "boolean");

    private final int code;
    private final String name;

    AttributeDataType(int code, String name) {
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
    public static AttributeDataType fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Attribute data type name cannot be null");
        }
        for (AttributeDataType type : AttributeDataType.values()) {
            if (type.name.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown attribute data type: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), AttributeDataType.class);
    }
}
