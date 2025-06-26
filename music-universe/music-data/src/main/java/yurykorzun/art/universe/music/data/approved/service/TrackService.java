package yurykorzun.art.universe.music.data.approved.service;

import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.TrackBindingRequestDTO;import yurykorzun.art.universe.music.data.approved.entity.DataSource;

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
     * Binds an external track to a track in the system.
     * If the track doesn't exist, it will be created.
     * The artist must be bound before binding the track.
     *
     * @param dataSource The external data source
     * @param externalId The ID of the track in the external system
     * @param request    The binding request containing track information
     * @return The created binding information
     * @throws IllegalStateException if the artist is not bound
     */
    BoundEntityProjection bindTrack(DataSource dataSource, Long externalId, TrackBindingRequestDTO request);
    
    /**
     * Unbinds an external track from the system.
     *
     * @param dataSource The external data source
     * @param externalId The ID of the track in the external system
     * @return true if the unbinding was successful, false otherwise
     */
    boolean unbindTrack(DataSource dataSource, Long externalId);

}
