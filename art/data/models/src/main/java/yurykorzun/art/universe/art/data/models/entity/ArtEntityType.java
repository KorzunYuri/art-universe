package yurykorzun.art.universe.art.data.models.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.domain.entity.EntityType;

import java.util.Arrays;

public enum ArtEntityType implements EntityType {
    PERSON(101, "person");

    private final int code;
    private final String name;

    ArtEntityType(int code, String name) {
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
    public static ArtEntityType fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Entity type name cannot be null");
        }

        for (ArtEntityType entityType : ArtEntityType.values()) {
            if (entityType.name.equalsIgnoreCase(name)) {
                return entityType;
            }
        }
        throw new IllegalArgumentException("Unknown entity type: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), ArtEntityType.class);
    }
}
