package yurykorzun.art.universe.music.data.raw.lastfm.common;

/**
 * Interface for deduplicating entities/dto in case of id absence.
 * Implementations in entity and DTO must comply to each other.
 */
public interface UniquenessSupport {
    String getUniqueKey();
}
