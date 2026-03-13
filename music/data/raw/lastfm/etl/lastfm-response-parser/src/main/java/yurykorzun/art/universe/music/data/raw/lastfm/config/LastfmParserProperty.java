package yurykorzun.art.universe.music.data.raw.lastfm.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum LastfmParserProperty implements ConfigurableProperty {

    SCHEDULE_DELAY_SECS(
        "lastfm.parser.schedule.delay-secs",
        PropertyType.INTEGER, "5",
        "Delay in seconds between response parsing runs",
        PropertyConstraints.ofRange(1, 300)
    ),
    ATTRIBUTE_HISTORY_DELAY_SECS(
        "lastfm.parser.attribute-history.delay-secs",
        PropertyType.INTEGER, "30",
        "Delay in seconds between attribute history processing runs",
        PropertyConstraints.ofRange(5, 3600)
    ),

    PARSE_ARTIST_GET_INFO(
        "lastfm.parser.parse.artist-get-info",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of ARTIST_GET_INFO responses",
        null
    ),
    PARSE_ARTIST_GET_SIMILAR(
        "lastfm.parser.parse.artist-get-similar",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of ARTIST_GET_SIMILAR responses",
        null
    ),
    PARSE_ARTIST_TOP_ALBUMS(
        "lastfm.parser.parse.artist-top-albums",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of ARTIST_TOP_ALBUMS responses",
        null
    ),
    PARSE_ARTIST_TOP_TRACKS(
        "lastfm.parser.parse.artist-top-tracks",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of ARTIST_TOP_TRACKS responses",
        null
    ),
    PARSE_ARTIST_TOP_TAGS(
        "lastfm.parser.parse.artist-top-tags",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of ARTIST_TOP_TAGS responses",
        null
    ),
    PARSE_ARTIST_SEARCH(
        "lastfm.parser.parse.artist-search",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of ARTIST_SEARCH responses",
        null
    ),
    PARSE_ALBUM_GET_INFO(
        "lastfm.parser.parse.album-get-info",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of ALBUM_GET_INFO responses",
        null
    ),
    PARSE_TRACK_GET_INFO(
        "lastfm.parser.parse.track-get-info",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of TRACK_GET_INFO responses",
        null
    ),
    PARSE_TAG_TOP_TAGS(
        "lastfm.parser.parse.tag-top-tags",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of TAG_TOP_TAGS responses",
        null
    ),
    PARSE_TAG_TOP_ARTISTS(
        "lastfm.parser.parse.tag-top-artists",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of TAG_TOP_ARTISTS responses",
        null
    ),
    PARSE_TAG_TOP_TRACKS(
        "lastfm.parser.parse.tag-top-tracks",
        PropertyType.BOOLEAN, "true",
        "Enable parsing of TAG_TOP_TRACKS responses",
        null
    ),

    THRESHOLD_ARTIST_LISTENERS_COUNT(
        "lastfm.parser.threshold.artist.listeners-count",
        PropertyType.INTEGER, "1000",
        "Minimum listeners count for an artist to be accepted",
        PropertyConstraints.ofRange(0, 10000000)
    ),
    THRESHOLD_ALBUM_PLAY_COUNT(
        "lastfm.parser.threshold.album.play-count",
        PropertyType.INTEGER, "10000",
        "Minimum play count for an album to be accepted",
        PropertyConstraints.ofRange(0, 100000000)
    ),
    THRESHOLD_TRACK_PLAY_COUNT(
        "lastfm.parser.threshold.track.play-count",
        PropertyType.INTEGER, "1000",
        "Minimum play count for a track to be accepted",
        PropertyConstraints.ofRange(0, 100000000)
    ),
    THRESHOLD_TAG_USAGE_COUNT(
        "lastfm.parser.threshold.tag.usage-count",
        PropertyType.INTEGER, "1000",
        "Minimum usage count for a tag to be accepted",
        PropertyConstraints.ofRange(0, 10000000)
    ),

    METHOD_ARTIST_GET_SIMILAR_MATCH_THRESHOLD(
        "lastfm.parser.method.artist-get-similar.match-threshold",
        PropertyType.DECIMAL, "0.2",
        "Minimum similarity match score for ARTIST_GET_SIMILAR",
        PropertyConstraints.ofRange(0.0, 1.0)
    ),
    METHOD_ARTIST_TOP_TAGS_USAGE_COUNT_THRESHOLD(
        "lastfm.parser.method.artist-top-tags.usage-count-threshold",
        PropertyType.INTEGER, "10",
        "Minimum tag usage count in ARTIST_TOP_TAGS response",
        PropertyConstraints.ofRange(0, 10000)
    ),
    METHOD_ARTIST_TOP_ALBUMS_PLAY_COUNT_THRESHOLD(
        "lastfm.parser.method.artist-top-albums.play-count-threshold",
        PropertyType.INTEGER, "10000",
        "Minimum play count for albums in ARTIST_TOP_ALBUMS response",
        PropertyConstraints.ofRange(0, 100000000)
    ),
    METHOD_ARTIST_SEARCH_SIMILARITY_THRESHOLD(
        "lastfm.parser.method.artist-search.similarity-threshold",
        PropertyType.DECIMAL, "0.5",
        "Minimum name similarity score to accept ARTIST_SEARCH result",
        PropertyConstraints.ofRange(0.0, 1.0)
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    LastfmParserProperty(String key, PropertyType propertyType, String defaultValue,
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
