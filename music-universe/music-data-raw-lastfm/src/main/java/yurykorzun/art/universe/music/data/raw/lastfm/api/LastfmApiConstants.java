package yurykorzun.art.universe.music.data.raw.lastfm.api;

public class LastfmApiConstants {

    private LastfmApiConstants() {}

    public static final int PAGE_SIZE = 50;

    //  common API parameters
    public static final String PARAM_NAME_OFFSET = "offset";
    public static final String PARAM_NAME_METHOD = "method";
    public static final String PARAM_NAME_API_KEY = "api_key";
    public static final String PARAM_NAME_FORMAT = "format";
    public static final String PARAM_NAME_PAGE = "page";
    public static final String PARAM_NAME_LIMIT = "limit"; // page size

    //  method-specific
    public static final String PARAM_NAME_TAG = "tag";

    //  default values for API parameters
    public static final String PARAM_DEFAULT_FORMAT = "json";

}
