package yurykorzun.art.universe.music.data.raw.lastfm.common;

/**
 * Interface for deduplicating entities/dto in case of id absence.
 */
public interface UniquenessSupport {
    String getUniqueKey();
}
