package yurykorzun.art.universe.common.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

/**
 * Enum representing entity types in the system
 */
public enum MasterEntityType implements EntityType {
    ARTIST(1, "artist"),
    ALBUM(2, "album"),
    TRACK(3, "track"),
    CATEGORY(4, "category"),
    PERSON(101, "person");

    private final int code;
    private final String name;

    MasterEntityType(int code, String name) {
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
    public static MasterEntityType fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Entity type name cannot be null");
        }

        for (MasterEntityType entityType : MasterEntityType.values()) {
            if (entityType.name.equalsIgnoreCase(name)) {
                return entityType;
            }
        }
        throw new IllegalArgumentException("Unknown entity type: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), MasterEntityType.class);
    }
}
