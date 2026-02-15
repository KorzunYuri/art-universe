package yurykorzun.art.universe.music.data.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.master.dto.AlbumDto;
import yurykorzun.art.universe.music.data.master.dto.AlbumSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.AlbumWithCategoriesDto;
import yurykorzun.art.universe.music.data.master.dto.AlbumWithTracksSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.ExternalAlbumWithTracksCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;

import java.util.List;

public interface AlbumService {

    Page<AlbumDto> findAlbums(String search, Long categoryId, Pageable pageable);

    Page<AlbumWithCategoriesDto> findAlbumsWithCategories(String search, Long categoryId, Pageable pageable);

    AlbumDto getAlbum(Long id);

    AlbumWithCategoriesDto getAlbumWithCategories(Long id);

    AlbumDto saveAlbum(AlbumSaveRequestDTO request);

    AlbumDto saveAlbumWithTracks(AlbumWithTracksSaveRequestDTO request);

    boolean deleteAlbum(Long id);

    void bindToCategory(Long albumId, Long categoryId);

    void unbindFromCategory(Long albumId, Long categoryId);

    List<BoundEntityProjection> findBoundAlbums(DataSource dataSource, List<Long> externalIds);
    
    /**
     * Binds an external album to an existing album in the system.
     * The artist must be bound before binding the album.
     *
     * @param dataSource The external data source
     * @param externalId The ID of the album in the external system
     * @param request    The binding request containing album ID and primary artist ID
     * @return The created binding information
     * @throws IllegalStateException if the artist is not bound
     * @throws jakarta.persistence.EntityNotFoundException if the album is not found
     */
    BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, ArtistRelatedEntityBindToExistingRequestDTO request);
    
    /**
     * Creates a new album and binds an external album to it.
     * The artist must be bound before binding the album.
     *
     * @param dataSource The external data source
     * @param externalId The ID of the album in the external system
     * @param request    The binding request containing album information and primary artist ID
     * @return The created binding information
     * @throws IllegalStateException if the artist is not bound or if album binding already exists
     * @throws IllegalArgumentException if an album with the same name and artist already exists
     */
    BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, ArtistRelatedEntityCreateAndBindRequestDTO request);

    /**
     * Creates a new master album from an external album together with its full tracklist.
     * Unbound tracks are created as new master tracks and bound to the provided external track IDs.
     * Bound tracks are linked to already-existing master tracks.
     *
     * @param dataSource      The external data source
     * @param externalAlbumId The ID of the album in the external system
     * @param request         The request containing album info and per-track binding details
     * @return The created album binding information
     */
    BoundEntityProjection createAndBindWithTracks(DataSource dataSource, Long externalAlbumId, ExternalAlbumWithTracksCreateAndBindRequestDTO request);

    /**
     * Unbinds an external album from the system.
     *
     * @param dataSource The external data source
     * @param externalId The ID of the album in the external system
     * @return true if the unbinding was successful, false otherwise
     */
    boolean unbindAlbum(DataSource dataSource, Long externalId);
    
    /**
     * Searches for albums by name (case insensitive, partial match) with optional artist filter.
     * Results are formatted as "Artist - Album" and prioritized by matching artist if specified.
     *
     * @param request The lookup request containing search term, optional artist IDs, and limit
     * @return List of album DTOs matching the search term
     */
    List<LookupResultDTO> lookupAlbums(ArtistRelatedLookupRequestDTO request);
    
    /**
     * Batch lookup of albums by multiple search terms with optional artist filter.
     * Each search term will have its own limited result set.
     *
     * @param request The batch lookup request containing search requests, artist IDs, and limit
     * @return A map of search terms to lists of matching albums
     */
    BatchLookupResponseDTO batchLookupAlbums(ArtistRelatedBatchLookupRequestDTO request);
}
