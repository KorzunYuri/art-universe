package yurykorzun.art.universe.music.data.approved.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

/**
 * Enum representing data sources in the system
 */
public enum DataSource implements Coded {
    LASTFM(1, "lastfm"),
    SPOTIFY(2, "spotify"),
    MUSICBRAINZ(3, "musicbrainz");

    private final int code;
    private final String name;

    DataSource(int code, String name) {
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
     * Converts string to DataSource
     * Used for JSON deserialization and path variable conversion
     *
     * @param name Data source name
     * @return DataSource instance
     * @throws IllegalArgumentException if name doesn't match any DataSource
     */
    @JsonCreator
    public static DataSource fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Data source name cannot be null");
        }
        
        for (DataSource dataSource : DataSource.values()) {
            if (dataSource.name.equalsIgnoreCase(name) || dataSource.name().equalsIgnoreCase(name)) {
                return dataSource;
            }
        }
        throw new IllegalArgumentException("Unknown data source: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), DataSource.class);
    }
}
