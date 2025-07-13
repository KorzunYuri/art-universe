package yurykorzun.art.universe.music.data.approved.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

/**
 * Enum representing entity types in the system
 */
public enum EntityType implements Coded {
    ARTIST(1, "artist"),
    ALBUM(2, "album"),
    TRACK(3, "track"),
    CATEGORY(4, "category");

    private final int code;
    private final String name;

    EntityType(int code, String name) {
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

    /**
     * Converts string to EntityType
     * Used for JSON deserialization and path variable conversion
     *
     * @param name Entity type name
     * @return EntityType instance
     * @throws IllegalArgumentException if name doesn't match any EntityType
     */
    @JsonCreator
    public static EntityType fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Entity type name cannot be null");
        }
        
        for (EntityType entityType : EntityType.values()) {
            if (entityType.name.equalsIgnoreCase(name)) {
                return entityType;
            }
        }
        throw new IllegalArgumentException("Unknown entity type: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), EntityType.class);
    }
}
