package yurykorzun.art.universe.music.data.raw.lastfm.common.utils;

import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;

public class StringUtils {

    private static final NormalizedStringSimilarity stringSimilarityCounter = new NormalizedLevenshtein();

    private StringUtils() {
    }

    /**
     * Calculates similarity coeff for two strings
     * @param s1
     * @param s2
     * @return
     */
    public static double getSimilarity(String s1, String s2) {
        return stringSimilarityCounter.similarity(s1, s2);
    }

}
