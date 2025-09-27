package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils;

public class DataQualityUtil {

    private DataQualityUtil() {
    }

    /**
     * Normalizes track URL by removing album information.
     * Converts https://www.last.fm/music/Artist/Album/Track
     * to https://www.last.fm/music/Artist/_/Track
     *
     * @param url original track URL
     * @return normalized track URL
     */
    public static String normalizeTrackUrl(String url) {
        if (url == null || !url.contains("/music/")) {
            return url;
        }

        String[] parts = url.split("/");
        if (parts.length >= 6) {
            // parts[0] = https:, parts[1] = "", parts[2] = www.last.fm,
            // parts[3] = music, parts[4] = Artist, parts[5] = Album/_, parts[6] = Track
            if (parts.length > 6) {
                // Replace album with underscore
                parts[5] = "_";
                return String.join("/", parts);
            }
        }
        return url;
    }

}
