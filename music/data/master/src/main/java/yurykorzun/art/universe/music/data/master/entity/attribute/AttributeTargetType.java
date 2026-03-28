package yurykorzun.art.universe.music.data.master.entity.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum AttributeTargetType implements Coded {
    // Entity types
    ARTIST(1, "artist"),
    ALBUM(2, "album"),
    TRACK(3, "track"),
    CATEGORY(4, "category"),
    PERSON(101, "person"),

    // Relation pair types
    ARTIST_ARTIST(10, "artist_artist"),
    ARTIST_ALBUM(11, "artist_album"),
    ARTIST_TRACK(12, "artist_track"),
    ARTIST_PERSON(13, "artist_person"),
    ALBUM_TRACK(14, "album_track"),
    ALBUM_PERSON(15, "album_person"),
    TRACK_TRACK(16, "track_track"),
    TRACK_PERSON(17, "track_person"),
    PERSON_PERSON(18, "person_person");

    private final int code;
    private final String name;

    AttributeTargetType(int code, String name) {
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

    public boolean isEntity() {
        return code < 10 || code > 99;
    }

    public boolean isRelation() {
        return code >= 10 && code <= 99;
    }

    @JsonCreator
    public static AttributeTargetType fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Attribute target type name cannot be null");
        }
        for (AttributeTargetType type : AttributeTargetType.values()) {
            if (type.name.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown attribute target type: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), AttributeTargetType.class);
    }
}
