package yurykorzun.art.universe.music.data.master.entity.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum AttributeSourceType implements Coded {
    MANUAL(1, "manual"),
    CALCULATED(2, "calculated"),
    LLM_EXTRACTED(3, "llm_extracted");

    private final int code;
    private final String name;

    AttributeSourceType(int code, String name) {
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
    public static AttributeSourceType fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Attribute source type name cannot be null");
        }
        for (AttributeSourceType type : AttributeSourceType.values()) {
            if (type.name.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown attribute source type: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), AttributeSourceType.class);
    }
}
