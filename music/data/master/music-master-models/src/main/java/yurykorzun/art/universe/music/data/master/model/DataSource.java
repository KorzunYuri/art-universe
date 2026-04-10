package yurykorzun.art.universe.music.data.master.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum DataSource implements Coded {
    LASTFM(1, "lastfm"),
    SPOTIFY(2, "spotify"),
    MUSICBRAINZ(3, "musicbrainz"),
    MASTER(4, "master");

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

    public static DataSource fromCode(int code) {
        for (DataSource dataSource : DataSource.values()) {
            if (dataSource.code == code) {
                return dataSource;
            }
        }
        throw new IllegalArgumentException("Unknown data source code: " + code);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), DataSource.class);
    }
}
