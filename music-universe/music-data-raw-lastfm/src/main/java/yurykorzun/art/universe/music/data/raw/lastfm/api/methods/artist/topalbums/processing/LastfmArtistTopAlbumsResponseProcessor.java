package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.processing;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.processing.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.dto.ArtistTopAlbumsAlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.dto.ArtistTopAlbumsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistAlbumService;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmArtistTopAlbumsResponseProcessor extends LastfmApiResponseProcessor<ArtistTopAlbumsDtoRoot> {

    private final LastfmArtistService artistService;
    private final LastfmAlbumService albumService;
    private final LastfmArtistAlbumService artistAlbumService;
    private final LastfmApiDtoProcessingService dtoProcessingService;

    @Value("${lastfm.client.methods.artist.topAlbums.albumPlayCountThreshold:10000}")
    private long albumPlayCountThreshold;

    // Artist attribute handlers
    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistDto>> artistAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmArtist, ArtistDto> artistFactory = 
            new EntityAttributeHandlerFactory<>(LastfmArtist.class, ArtistDto.class);
        artistAttrHandlers = List.of(
            artistFactory.createHandler(LastfmAttribute.URL, false, "url"),
            artistFactory.createHandler(LastfmAttribute.MBID, false, "mbid")
        );
    }

    // Album attribute handlers
    private static final List<EntityAttributeHandler<LastfmAlbum, ?, ArtistTopAlbumsAlbumDto>> albumAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmAlbum, ArtistTopAlbumsAlbumDto> albumFactory = 
            new EntityAttributeHandlerFactory<>(LastfmAlbum.class, ArtistTopAlbumsAlbumDto.class);
        albumAttrHandlers = List.of(
            albumFactory.createHandler(LastfmAttribute.URL, false, "url"),
            albumFactory.createHandler(LastfmAttribute.MBID, false, "mbid"),
            albumFactory.createHandler(LastfmAttribute.PLAY_COUNT, false, "playCount")
        );
    }

    protected LastfmArtistTopAlbumsResponseProcessor(
        LastfmArtistService artistService,
        LastfmAlbumService albumService,
        LastfmArtistAlbumService artistAlbumService,
        LastfmApiDtoProcessingService dtoProcessingService
    ) {
        super(ArtistTopAlbumsDtoRoot.class);

        this.artistService = artistService;
        this.albumService = albumService;
        this.artistAlbumService = artistAlbumService;
        this.dtoProcessingService = dtoProcessingService;
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_ALBUMS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {
        ArtistTopAlbumsDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();
        
        // Verify source artist exists (but we won't use it for album creation)
        LastfmArtist sourceArtist = artistService.findById(sourceApiCall.getEntityId())
            .orElseThrow(() -> new EntityNotFoundException(
                String.format("Source artist with ID=%s not found", sourceApiCall.getEntityId())));

        // Filter albums by threshold and artist match
        List<ArtistTopAlbumsAlbumDto> filteredAlbums = getAlbumsToSave(dtoRoot, sourceArtist);
        if (filteredAlbums.isEmpty()) {
            log.info("No albums to process after filtering");
            return;
        }

        // Step 1: Extract all artists from album DTOs
        List<ArtistDto> artistDtos = extractArtistsFromAlbums(filteredAlbums);
        
        // Step 2: Process artists first
        var artistsResult = processArtists(artistDtos, sourceApiCall);
        Map<String, LastfmArtist> artistsByName = createArtistNameMap(artistsResult.actualEntities());
        
        // Step 3: Process albums with artist references
        var albumsResult = processAlbums(filteredAlbums, sourceApiCall, artistsByName);
        
        // Step 4: Create artist-album relationships for consistency
        createArtistAlbumRelationships(albumsResult, sourceApiCall, artistsByName);
    }

    private List<ArtistDto> extractArtistsFromAlbums(List<ArtistTopAlbumsAlbumDto> albums) {
        return albums.stream()
            .map(ArtistTopAlbumsAlbumDto::getArtist)
            .distinct()
            .toList();
    }

    private LastfmApiDtoProcessingResult<LastfmArtist, ArtistDto> processArtists(
        List<ArtistDto> artistDtos, 
        LastfmApiCall sourceApiCall
    ) {
        // Deduplicate artists by MBID if available, otherwise by name
        Map<String, ArtistDto> uniqueArtists = new HashMap<>();
        
        for (ArtistDto dto : artistDtos) {
            String key = (dto.getMbid() != null && !dto.getMbid().isEmpty()) 
                ? dto.getMbid() 
                : dto.getName();
                
            // Only add if not already present or if this one has an MBID and the existing one doesn't
            if (!uniqueArtists.containsKey(key) || 
                (dto.getMbid() != null && !dto.getMbid().isEmpty() && 
                 (uniqueArtists.get(key).getMbid() == null || uniqueArtists.get(key).getMbid().isEmpty()))) {
                uniqueArtists.put(key, dto);
            }
        }
        
        List<ArtistDto> dedupedArtists = new ArrayList<>(uniqueArtists.values());
        log.info("Processing {} unique artists from album DTOs", dedupedArtists.size());
        
        // Create artist factory
        EntityFactory<LastfmArtist, ArtistDto> artistFactory = new LastfmArtistEntityFactory<>();
        
        // Process artists
        LastfmApiDtoProcessingResult<LastfmArtist, ArtistDto> result = dtoProcessingService.process(
            sourceApiCall,
            dedupedArtists,
            artistFactory,
            artistAttrHandlers,
            artistService
        );
        
        log.info("Processed {} artists", result.actualEntities().size());
        return result;
    }

    private Map<String, LastfmArtist> createArtistNameMap(List<LastfmArtist> artists) {
        return artists.stream()
            .collect(Collectors.toMap(
                LastfmArtist::getName,
                Function.identity(),
                // If duplicate names, keep the one with MBID if possible
                (a1, a2) -> (a1.getMbid() != null && !a1.getMbid().isEmpty()) ? a1 : a2
            ));
    }

    private LastfmApiDtoProcessingResult<LastfmAlbum, ArtistTopAlbumsAlbumDto> processAlbums(
        List<ArtistTopAlbumsAlbumDto> albumDtos,
        LastfmApiCall sourceApiCall,
        Map<String, LastfmArtist> artistsByName
    ) {
        log.info("Processing {} albums", albumDtos.size());
        
        // Create album factory with artist map
        EntityFactory<LastfmAlbum, ArtistTopAlbumsAlbumDto> albumFactory = 
            new LastfmArtistTopAlbumsAlbumFactory(artistsByName);
        
        // Process albums
        LastfmApiDtoProcessingResult<LastfmAlbum, ArtistTopAlbumsAlbumDto> result = dtoProcessingService.process(
            sourceApiCall,
            albumDtos,
            albumFactory,
            albumAttrHandlers,
            albumService
        );
        
        log.info("Processed {} albums", result.actualEntities().size());
        return result;
    }

    private void createArtistAlbumRelationships(
        LastfmApiDtoProcessingResult<LastfmAlbum, ArtistTopAlbumsAlbumDto> albumsResult,
        LastfmApiCall sourceApiCall,
        Map<String, LastfmArtist> artistsByName
    ) {
        List<LastfmArtistAlbum> relations = new ArrayList<>();
        
        albumsResult.entityMapping().values().forEach(mapping -> {
            LastfmAlbum album = mapping.getNewEntity();
            ArtistTopAlbumsAlbumDto dto = mapping.getDto();
            
            // get artist by name
            if (dto.getArtist() != null && dto.getArtist().getName() != null) {
                LastfmArtist artist = artistsByName.get(dto.getArtist().getName());
                if (artist == null) {
                    log.warn("Artist not found for album: {}", album.getName());
                    return;
                }

                // create relation
                LastfmArtistAlbum relation = LastfmArtistAlbum.builder()
                    .apiCall(sourceApiCall)
                    .artist(artist)
                    .album(album)
                    .build();
                    
                relations.add(relation);
            }
        });

        if (!relations.isEmpty()) {
            artistAlbumService.upsertAll(relations);
            log.info("Saved {} artist-album relations", relations.size());
        }
    }

    private List<ArtistTopAlbumsAlbumDto> getAlbumsToSave(ArtistTopAlbumsDtoRoot dtoRoot, LastfmArtist sourceArtist) {
        if (dtoRoot.getTopAlbumsObject() == null || dtoRoot.getTopAlbumsObject().getAlbums() == null) {
            return Collections.emptyList();
        }
        
        return dtoRoot.getTopAlbumsObject().getAlbums().stream()
            .filter(albumFilter(sourceArtist))
            .toList();
    }

    private Predicate<ArtistTopAlbumsAlbumDto> albumFilter(LastfmArtist sourceArtist) {
        return dto -> {
            if (dto.getPlayCount() < albumPlayCountThreshold) {
                return false;
            }

            return true;
        };
    }
}
