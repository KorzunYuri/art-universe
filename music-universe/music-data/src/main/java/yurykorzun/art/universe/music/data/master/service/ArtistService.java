package yurykorzun.art.universe.music.data.master.service;

import yurykorzun.art.universe.music.data.master.dto.lookup.*;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.DataSource;

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
    BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, EntityBindToExistingRequestDTO request);
    
    /**
     * Create a new artist and bind an external artist to it
     * 
     * @param dataSource The external data source
     * @param externalId The ID of the artist in the external system
     * @param request The binding request containing artist information
     * @return The created binding information
     */
    BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, EntityCreateAndBindRequestDTO request);
    
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
     * @param request The lookup request containing search term and optional limit
     * @return List of artist DTOs matching the search term
     */
    List<LookupResultDTO> lookupArtists(LookupRequestDTO request);

    /**
     * Batch lookup of artists by multiple search terms.
     * Each search term will have its own limited result set.
     *
     * @param request The batch lookup request containing search requests and limit
     * @return A map of search terms to lists of matching artists
     */
    BatchLookupResponseDTO batchLookupArtists(BaseBatchLookupRequestDTO request);
}
