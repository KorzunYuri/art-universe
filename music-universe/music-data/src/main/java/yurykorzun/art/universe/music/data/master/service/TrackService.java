package yurykorzun.art.universe.music.data.master.service;

import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;

import java.util.List;

public interface TrackService {

    List<BoundEntityProjection> findBoundTracks(DataSource dataSource, List<Long> externalIds);
    
    /**
     * Finds a single bound track by data source and external ID.
     *
     * @param dataSource The external data source
     * @param externalId The ID of the track in the external system
     * @return The bound track information, or null if not found
     */
    BoundEntityProjection findTrack(DataSource dataSource, Long externalId);
    
    /**
     * Binds an external track to an existing track in the system.
     * The artist must be bound before binding the track.
     *
     * @param dataSource The external data source
     * @param externalId The ID of the track in the external system
     * @param request    The binding request containing track ID and primary artist ID
     * @return The created binding information
     * @throws IllegalStateException if the artist is not bound
     * @throws jakarta.persistence.EntityNotFoundException if the track is not found
     */
    BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, ArtistRelatedEntityBindToExistingRequestDTO request);
    
    /**
     * Creates a new track and binds an external track to it.
     * The artist must be bound before binding the track.
     *
     * @param dataSource The external data source
     * @param externalId The ID of the track in the external system
     * @param request    The binding request containing track information and primary artist ID
     * @return The created binding information
     * @throws IllegalStateException if the artist is not bound or if track binding already exists
     * @throws IllegalArgumentException if a track with the same name and artist already exists
     */
    BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, ArtistRelatedEntityCreateAndBindRequestDTO request);
    
    /**
     * Unbinds an external track from the system.
     *
     * @param dataSource The external data source
     * @param externalId The ID of the track in the external system
     * @return true if the unbinding was successful, false otherwise
     */
    boolean unbindTrack(DataSource dataSource, Long externalId);
    
    /**
     * Searches for tracks by name (case insensitive, partial match) with optional artist filter.
     * Results are formatted as "Artist - Track" and prioritized by matching artist if specified.
     *
     * @param request The lookup request containing search term, optional artist IDs, and limit
     * @return List of track DTOs matching the search term
     */
    List<LookupResultDTO> lookupTracks(ArtistRelatedLookupRequestDTO request);

    /**
     * Batch lookup of tracks by multiple search terms with optional artist filter.
     * Each search term will have its own limited result set.
     *
     * @param request The batch lookup request containing search requests, artist IDs, and limit
     * @return A map of search terms to lists of matching tracks
     */
    BatchLookupResponseDTO batchLookupTracks(ArtistRelatedBatchLookupRequestDTO request);
}
