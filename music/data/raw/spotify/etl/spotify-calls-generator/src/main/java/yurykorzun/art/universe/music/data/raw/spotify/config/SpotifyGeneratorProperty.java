package yurykorzun.art.universe.music.data.raw.spotify.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum SpotifyGeneratorProperty implements ConfigurableProperty {

    SCHEDULE_DELAY_SECS(
        "spotify.generator.schedule.delay-secs",
        PropertyType.INTEGER, "60",
        "Delay in seconds between API call generation runs",
        PropertyConstraints.ofRange(1, 3600)
    ),

    GENERATE_ARTIST_GET(
        "spotify.generator.generate.artist-get",
        PropertyType.BOOLEAN, "true",
        "Enable generation of ARTIST_GET calls",
        null
    ),
    GENERATE_ARTIST_ALBUMS(
        "spotify.generator.generate.artist-albums",
        PropertyType.BOOLEAN, "true",
        "Enable generation of ARTIST_ALBUMS calls",
        null
    ),
    GENERATE_ALBUM_GET(
        "spotify.generator.generate.album-get",
        PropertyType.BOOLEAN, "true",
        "Enable generation of ALBUM_GET calls",
        null
    ),
    GENERATE_ALBUM_TRACKS(
        "spotify.generator.generate.album-tracks",
        PropertyType.BOOLEAN, "true",
        "Enable generation of ALBUM_TRACKS calls",
        null
    ),
    GENERATE_TRACK_GET(
        "spotify.generator.generate.track-get",
        PropertyType.BOOLEAN, "true",
        "Enable generation of TRACK_GET calls",
        null
    ),
    GENERATE_SEARCH_ARTIST(
        "spotify.generator.generate.search-artist",
        PropertyType.BOOLEAN, "false",
        "Enable generation of SEARCH_ARTIST calls",
        null
    ),
    GENERATE_SEARCH_ALBUM(
        "spotify.generator.generate.search-album",
        PropertyType.BOOLEAN, "false",
        "Enable generation of SEARCH_ALBUM calls",
        null
    ),
    GENERATE_SEARCH_TRACK(
        "spotify.generator.generate.search-track",
        PropertyType.BOOLEAN, "false",
        "Enable generation of SEARCH_TRACK calls",
        null
    ),

    DUE_DURATION_ARTIST_GET(
        "spotify.generator.due-duration-days.artist-get",
        PropertyType.INTEGER, "30",
        "Due duration days for ARTIST_GET calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_ARTIST_ALBUMS(
        "spotify.generator.due-duration-days.artist-albums",
        PropertyType.INTEGER, "30",
        "Due duration days for ARTIST_ALBUMS calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_ALBUM_GET(
        "spotify.generator.due-duration-days.album-get",
        PropertyType.INTEGER, "30",
        "Due duration days for ALBUM_GET calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_ALBUM_TRACKS(
        "spotify.generator.due-duration-days.album-tracks",
        PropertyType.INTEGER, "30",
        "Due duration days for ALBUM_TRACKS calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_TRACK_GET(
        "spotify.generator.due-duration-days.track-get",
        PropertyType.INTEGER, "30",
        "Due duration days for TRACK_GET calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_SEARCH(
        "spotify.generator.due-duration-days.search",
        PropertyType.INTEGER, "7",
        "Due duration days for SEARCH_* calls",
        PropertyConstraints.ofRange(1, 365)
    ),

    SEARCH_BATCH_SIZE(
        "spotify.generator.search.batch-size",
        PropertyType.INTEGER, "100",
        "Batch size for search call generation",
        PropertyConstraints.ofRange(1, 1000)
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    SpotifyGeneratorProperty(String key, PropertyType propertyType, String defaultValue,
                             String description, PropertyConstraints constraints) {
        this.key = key;
        this.propertyType = propertyType;
        this.defaultValue = defaultValue;
        this.description = description;
        this.constraints = constraints;
    }

    @Override public String getKey() { return key; }
    @Override public PropertyType getPropertyType() { return propertyType; }
    @Override public String getDefaultValue() { return defaultValue; }
    @Override public String getDescription() { return description; }
    @Override public PropertyConstraints getConstraints() { return constraints; }
}
