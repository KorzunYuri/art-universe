package yurykorzun.art.universe.music.data.raw.spotify.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum SpotifyParserProperty implements ConfigurableProperty {

    SCHEDULE_DELAY_SECS(
        "spotify.parser.schedule.delay-secs",
        PropertyType.INTEGER, "5",
        "Delay in seconds between response parsing runs",
        PropertyConstraints.ofRange(1, 300)
    ),

    PARSE_ARTIST_GET(
        "spotify.parser.parse.artist-get",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of ARTIST_GET responses",
        null
    ),
    PARSE_ARTIST_ALBUMS(
        "spotify.parser.parse.artist-albums",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of ARTIST_ALBUMS responses",
        null
    ),
    PARSE_ALBUM_GET(
        "spotify.parser.parse.album-get",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of ALBUM_GET responses",
        null
    ),
    PARSE_ALBUM_TRACKS(
        "spotify.parser.parse.album-tracks",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of ALBUM_TRACKS responses",
        null
    ),
    PARSE_TRACK_GET(
        "spotify.parser.parse.track-get",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of TRACK_GET responses",
        null
    ),
    PARSE_SEARCH_ARTIST(
        "spotify.parser.parse.search-artist",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of SEARCH_ARTIST responses",
        null
    ),
    PARSE_SEARCH_ALBUM(
        "spotify.parser.parse.search-album",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of SEARCH_ALBUM responses",
        null
    ),
    PARSE_SEARCH_TRACK(
        "spotify.parser.parse.search-track",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of SEARCH_TRACK responses",
        null
    ),

    STAGING_ITERATION_MAX_RECORDS(
        "spotify.parser.staging.iteration-max-records",
        PropertyType.INTEGER, "5000",
        "Maximum records per staging iteration before sealing",
        PropertyConstraints.ofRange(100, 100000)
    ),
    STAGING_ITERATION_MAX_OPEN_MINUTES(
        "spotify.parser.staging.iteration-max-open-minutes",
        PropertyType.INTEGER, "5",
        "Maximum minutes a staging iteration can remain open",
        PropertyConstraints.ofRange(1, 1440)
    ),

    SEARCH_MATCH_THRESHOLD(
        "spotify.parser.search.match-threshold",
        PropertyType.DECIMAL, "0.85",
        "Minimum Jaro-Winkler similarity score to accept a search match",
        PropertyConstraints.ofRange(0.0, 1.0)
    ),
    SEARCH_GRACE_PERIOD_DAYS(
        "spotify.parser.search.grace-period-days",
        PropertyType.INTEGER, "30",
        "Days before retrying a failed search attempt",
        PropertyConstraints.ofRange(1, 365)
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    SpotifyParserProperty(String key, PropertyType propertyType, String defaultValue,
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
