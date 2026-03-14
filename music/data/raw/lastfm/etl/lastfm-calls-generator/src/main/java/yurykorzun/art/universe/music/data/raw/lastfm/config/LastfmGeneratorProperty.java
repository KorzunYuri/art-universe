package yurykorzun.art.universe.music.data.raw.lastfm.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum LastfmGeneratorProperty implements ConfigurableProperty {

    SCHEDULE_DELAY_SECS(
        "lastfm.generator.schedule.delay-secs",
        PropertyType.INTEGER, "60",
        "Delay in seconds between API call generation runs",
        PropertyConstraints.ofRange(1, 3600)
    ),

    GENERATE_ARTIST_GET_INFO(
        "lastfm.generator.generate.artist-get-info",
        PropertyType.BOOLEAN, "true",
        "Enable generation of ARTIST_GET_INFO calls",
        null
    ),
    GENERATE_ARTIST_GET_SIMILAR(
        "lastfm.generator.generate.artist-get-similar",
        PropertyType.BOOLEAN, "true",
        "Enable generation of ARTIST_GET_SIMILAR calls",
        null
    ),
    GENERATE_ARTIST_TOP_ALBUMS(
        "lastfm.generator.generate.artist-top-albums",
        PropertyType.BOOLEAN, "true",
        "Enable generation of ARTIST_TOP_ALBUMS calls",
        null
    ),
    GENERATE_ARTIST_TOP_TRACKS(
        "lastfm.generator.generate.artist-top-tracks",
        PropertyType.BOOLEAN, "true",
        "Enable generation of ARTIST_TOP_TRACKS calls",
        null
    ),
    GENERATE_ARTIST_TOP_TAGS(
        "lastfm.generator.generate.artist-top-tags",
        PropertyType.BOOLEAN, "true",
        "Enable generation of ARTIST_TOP_TAGS calls",
        null
    ),
    GENERATE_ALBUM_GET_INFO(
        "lastfm.generator.generate.album-get-info",
        PropertyType.BOOLEAN, "true",
        "Enable generation of ALBUM_GET_INFO calls",
        null
    ),
    GENERATE_TRACK_GET_INFO(
        "lastfm.generator.generate.track-get-info",
        PropertyType.BOOLEAN, "true",
        "Enable generation of TRACK_GET_INFO calls",
        null
    ),
    GENERATE_TAG_TOP_TAGS(
        "lastfm.generator.generate.tag-top-tags",
        PropertyType.BOOLEAN, "true",
        "Enable generation of TAG_TOP_TAGS calls",
        null
    ),
    GENERATE_TAG_TOP_ARTISTS(
        "lastfm.generator.generate.tag-top-artists",
        PropertyType.BOOLEAN, "true",
        "Enable generation of TAG_TOP_ARTISTS calls",
        null
    ),
    GENERATE_TAG_TOP_TRACKS(
        "lastfm.generator.generate.tag-top-tracks",
        PropertyType.BOOLEAN, "true",
        "Enable generation of TAG_TOP_TRACKS calls",
        null
    ),
    GENERATE_ARTIST_SEARCH(
        "lastfm.generator.generate.artist-search",
        PropertyType.BOOLEAN, "true",
        "Enable generation of ARTIST_SEARCH calls",
        null
    ),

    DUE_DURATION_ARTIST_GET_INFO(
        "lastfm.generator.due-duration-days.artist-get-info",
        PropertyType.INTEGER, "7",
        "Due duration days for ARTIST_GET_INFO calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_ARTIST_GET_SIMILAR(
        "lastfm.generator.due-duration-days.artist-get-similar",
        PropertyType.INTEGER, "28",
        "Due duration days for ARTIST_GET_SIMILAR calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_ARTIST_TOP_ALBUMS(
        "lastfm.generator.due-duration-days.artist-top-albums",
        PropertyType.INTEGER, "28",
        "Due duration days for ARTIST_TOP_ALBUMS calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_ARTIST_TOP_TRACKS(
        "lastfm.generator.due-duration-days.artist-top-tracks",
        PropertyType.INTEGER, "28",
        "Due duration days for ARTIST_TOP_TRACKS calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_ARTIST_TOP_TAGS(
        "lastfm.generator.due-duration-days.artist-top-tags",
        PropertyType.INTEGER, "7",
        "Due duration days for ARTIST_TOP_TAGS calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_ALBUM_GET_INFO(
        "lastfm.generator.due-duration-days.album-get-info",
        PropertyType.INTEGER, "28",
        "Due duration days for ALBUM_GET_INFO calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_TRACK_GET_INFO(
        "lastfm.generator.due-duration-days.track-get-info",
        PropertyType.INTEGER, "56",
        "Due duration days for TRACK_GET_INFO calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_TAG_TOP_TAGS(
        "lastfm.generator.due-duration-days.tag-top-tags",
        PropertyType.INTEGER, "7",
        "Due duration days for TAG_TOP_TAGS calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_TAG_TOP_ARTISTS(
        "lastfm.generator.due-duration-days.tag-top-artists",
        PropertyType.INTEGER, "7",
        "Due duration days for TAG_TOP_ARTISTS calls",
        PropertyConstraints.ofRange(1, 365)
    ),
    DUE_DURATION_TAG_TOP_TRACKS(
        "lastfm.generator.due-duration-days.tag-top-tracks",
        PropertyType.INTEGER, "7",
        "Due duration days for TAG_TOP_TRACKS calls",
        PropertyConstraints.ofRange(1, 365)
    ),

    RECORDS_LIMIT_TAG_TOP_TAGS(
        "lastfm.generator.records-limit.tag-top-tags",
        PropertyType.INTEGER, "2000",
        "Max records for TAG_TOP_TAGS (API offset breaks after 2000)",
        PropertyConstraints.ofRange(100, 10000)
    ),
    USAGE_TO_PAGE_RATIO_TAG_TOP_TRACKS(
        "lastfm.generator.usage-to-page-ratio.tag-top-tracks",
        PropertyType.INTEGER, "50000",
        "For each N usages of a tag, one 50-item API call is generated",
        PropertyConstraints.ofRange(1000, 500000)
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    LastfmGeneratorProperty(String key, PropertyType propertyType, String defaultValue,
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
