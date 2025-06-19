package yurykorzun.art.universe.music.data.approved.service;

import yurykorzun.art.universe.music.data.approved.dto.ArtistBindingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;

import java.util.List;

public interface ArtistService {

    List<BoundEntityProjection> findBoundArtists(DataSource dataSource, List<Long> externalIds);
    
    /**
     * Binds an external artist to an artist in the system.
     * If the artist doesn't exist, it will be created.
     *
     * @param dataSource The external data source
     * @param externalId The ID of the artist in the external system
     * @param request    The binding request containing artist information
     * @return The created binding information
     */
    BoundEntityProjection bindArtist(DataSource dataSource, Long externalId, ArtistBindingRequestDTO request);
    
    /**
     * Unbinds an external artist from the system.
     *
     * @param dataSource The external data source
     * @param externalId The ID of the artist in the external system
     * @return true if the unbinding was successful, false otherwise
     */
    boolean unbindArtist(DataSource dataSource, Long externalId);
}
