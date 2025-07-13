package yurykorzun.art.universe.music.data.approved.service;

import yurykorzun.art.universe.music.data.approved.dto.ArtistBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.ArtistBatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.dto.ArtistBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.ArtistCreateAndBindRequestDTO;
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
     * Bind an external artist to an existing artist in the system
     * 
     * @param dataSource The external data source
     * @param externalId The ID of the artist in the external system
     * @param request The binding request containing artist ID
     * @return The created binding information
     */
    BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, ArtistBindToExistingRequestDTO request);
    
    /**
     * Create a new artist and bind an external artist to it
     * 
     * @param dataSource The external data source
     * @param externalId The ID of the artist in the external system
     * @param request The binding request containing artist information
     * @return The created binding information
     */
    BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, ArtistCreateAndBindRequestDTO request);
    
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
    List<LookupResultDTO> searchArtistsByName(String search, Integer limit);
    
    /**
     * Searches for artists by name (case insensitive, partial match).
     * Uses default limit of 20 results.
     *
     * @param search The search term to look for in artist names
     * @return List of artist DTOs matching the search term
     */
    default List<LookupResultDTO> searchArtistsByName(String search) {
        return searchArtistsByName(search, 20);
    }
    
    /**
     * Batch lookup of artists by multiple search terms.
     * Each search term will have its own limited result set.
     *
     * @param request The batch lookup request containing search terms and limit
     * @return A map of search terms to lists of matching artists
     */
    ArtistBatchLookupResponseDTO batchLookupArtists(ArtistBatchLookupRequestDTO request);
}
