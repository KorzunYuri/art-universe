package yurykorzun.art.universe.music.data.raw.lastfm.api;

public class LastfmApiConstants {

    private LastfmApiConstants() {}

    public static final int PAGE_SIZE = 50;

    //  common API parameters
    public static final String PARAM_NAME_OFFSET = "offset";
    public static final String PARAM_NAME_METHOD = "method";
    public static final String PARAM_NAME_MBID = "mbid";
    public static final String PARAM_NAME_AUTOCORRECT = "autocorrect";
    public static final String PARAM_NAME_API_KEY = "api_key";
    public static final String PARAM_NAME_FORMAT = "format";
    public static final String PARAM_NAME_PAGE = "page"; // TODO consider renaming
    public static final String PARAM_NAME_LIMIT = "limit"; // page size TODO consider renaming

    //  method-specific
    public static final String PARAM_NAME_TAG = "tag";
    public static final String PARAM_NAME_ARTIST = "artist";
    public static final String PARAM_NAME_ALBUM = "album";
    public static final String PARAM_NAME_TRACK = "track";

    //  default values for API parameters
    public static final String PARAM_DEFAULT_FORMAT = "json";

}
