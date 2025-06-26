package yurykorzun.art.universe.music.data.approved.service;

import yurykorzun.art.universe.music.data.approved.dto.ArtistBindingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.ArtistSearchResultDTO;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;

import java.util.List;

public interface ArtistService {

    List<BoundEntityProjection> findBoundArtists(DataSource dataSource, List<Long> externalIds);
    
    /**
     * Finds a single bound artist by data source and external ID.
     *
     * @param dataSource The external data source
     * @param externalId The ID of the artist in the external system
     * @return The bound artist information, or null if not found
     */
    BoundEntityProjection findArtist(DataSource dataSource, Long externalId);
    
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
    
    /**
     * Searches for artists by name (case insensitive, partial match).
     *
     * @param search The search term to look for in artist names
     * @param limit Maximum number of results to return (default: 20)
     * @return List of artist DTOs matching the search term
     */
    List<ArtistSearchResultDTO> searchArtistsByName(String search, Integer limit);
    
    /**
     * Searches for artists by name (case insensitive, partial match).
     * Uses default limit of 20 results.
     *
     * @param search The search term to look for in artist names
     * @return List of artist DTOs matching the search term
     */
    default List<ArtistSearchResultDTO> searchArtistsByName(String search) {
        return searchArtistsByName(search, 20);
    }
}
