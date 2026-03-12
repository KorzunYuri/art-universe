package yurykorzun.art.universe.music.data.raw.spotify.enums;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

@Getter
public enum SpotifyDatePrecision implements Coded {

    YEAR(1),
    MONTH(2),
    DAY(3);

    private final int code;

    SpotifyDatePrecision(int code) {
        this.code = code;
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), SpotifyDatePrecision.class);
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name();
    }

    public static SpotifyDatePrecision fromSpotifyString(String precision) {
        if (precision == null) return null;
        return switch (precision.toLowerCase()) {
            case "year" -> YEAR;
            case "month" -> MONTH;
            case "day" -> DAY;
            default -> null;
        };
    }
}
