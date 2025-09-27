package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistAlbum;

import java.util.List;

public interface LastfmArtistAlbumRepository extends JpaRepository<LastfmArtistAlbum, Long> {
    
    /**
     * Find all artist-album relationships for a specific album
     * 
     * @param albumId The ID of the album
     * @return List of artist-album relationships
     */
    List<LastfmArtistAlbum> findByAlbumId(long albumId);
}
