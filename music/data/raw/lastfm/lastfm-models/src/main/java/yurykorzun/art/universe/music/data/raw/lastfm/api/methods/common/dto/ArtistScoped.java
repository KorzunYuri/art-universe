package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto;

import jakarta.annotation.Nullable;

import java.beans.Transient;

/**
 * Interface for providing unique key generation logic for track entities and DTOs.
 * Implements a hierarchical key strategy: MBID → name+artist → normalized URL.
 */
public interface ArtistScoped {

    @Transient
    @Nullable
    String getArtistName();
}
