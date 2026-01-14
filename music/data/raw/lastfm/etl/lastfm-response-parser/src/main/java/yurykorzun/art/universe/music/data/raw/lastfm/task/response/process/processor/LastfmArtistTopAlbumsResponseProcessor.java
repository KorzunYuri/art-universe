package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.config.MappingConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.topalbums.ArtistTopAlbumsAlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.topalbums.ArtistTopAlbumsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.topalbums.LastfmArtistTopAlbumsAlbumFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingOrchestrator;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils.dedup.AlbumDeduplicationUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.LastfmArtistAlbumService;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmArtistTopAlbumsResponseProcessor extends LastfmApiResponseProcessor<ArtistTopAlbumsDtoRoot> {

    private final LastfmArtistService artistService;
    private final LastfmAlbumService albumService;
    private final LastfmArtistAlbumService artistAlbumService;
    private final LastfmApiDtoProcessingOrchestrator dtoProcessingService;
    private final DtoQualityService dtoQualityService;

    @Value("${lastfm.tasks.response-parse.methods.artist.top-albums.album-play-count-threshold}")
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
        LastfmApiDtoProcessingOrchestrator dtoProcessingService,
        DtoQualityService dtoQualityService,
        @Qualifier(MappingConfig.LASTFM_API_RESPONSE_OBJECT_MAPPER_BEAN_NAME) ObjectMapper objectMapper
    ) {
        super(ArtistTopAlbumsDtoRoot.class, objectMapper);

        this.artistService = artistService;
        this.albumService = albumService;
        this.artistAlbumService = artistAlbumService;
        this.dtoProcessingService = dtoProcessingService;
        this.dtoQualityService = dtoQualityService;
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

        // Step 1: Filter and validate albums
        List<ArtistTopAlbumsAlbumDto> qualityAlbums = getQualityAlbums(dtoRoot);
        if (qualityAlbums.isEmpty()) {
            log.info("No quality albums found for artist {} after validation", sourceArtist.getName());
            return;
        }

        // Step 2: Extract and validate artists from albums
        List<ArtistDto> qualityArtists = extractQualityArtists(qualityAlbums);
        if (qualityArtists.isEmpty()) {
            log.info("No quality artists found after validation");
            return;
        }
        
        // Step 3: Process artists first
        var artistsResult = processArtists(qualityArtists, sourceApiCall);
        Map<String, LastfmArtist> artistsByName = createArtistNameMap(artistsResult.actualEntities());
        
        // Step 4: Process albums with artist references
        var albumsToProcess = filterAlbumsWithValidArtists(qualityAlbums, artistsByName);
        var albumsResult = processAlbums(albumsToProcess, sourceApiCall, artistsByName);
        
        // Step 5: Create artist-album relationships
        createArtistAlbumRelationships(albumsResult, sourceApiCall, artistsByName);
    }

    /**
     * Filters and validates albums by quality metrics and blacklist
     */
    private List<ArtistTopAlbumsAlbumDto> getQualityAlbums(ArtistTopAlbumsDtoRoot dtoRoot) {
        if (dtoRoot.getTopAlbumsObject() == null || dtoRoot.getTopAlbumsObject().getAlbums() == null) {
            return Collections.emptyList();
        }
        
        List<ArtistTopAlbumsAlbumDto> allAlbums = AlbumDeduplicationUtils.deduplicateAlbumDtos(dtoRoot.getTopAlbumsObject().getAlbums());

        // First validate against blacklist and metrics
        var qualityAlbums = dtoQualityService.validateAndBlacklist(allAlbums)
            .stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .toList();

        // Then filter by play count threshold
        var thresholdFilteredAlbums = qualityAlbums.stream()
            .filter(dto -> dto.getPlayCount() > albumPlayCountThreshold)
            .toList();

        if (thresholdFilteredAlbums.size() < allAlbums.size()) {
            log.info("Filtered {} albums: {} failed validation, {} below play count threshold", 
                allAlbums.size() - thresholdFilteredAlbums.size(),
                allAlbums.size() - qualityAlbums.size(),
                qualityAlbums.size() - thresholdFilteredAlbums.size());
        }

        return thresholdFilteredAlbums;
    }

    /**
     * Extracts unique artists from albums and validates them against blacklist
     */
    private List<ArtistDto> extractQualityArtists(List<ArtistTopAlbumsAlbumDto> albums) {
        List<ArtistDto> artistDtos = albums.stream()
            .map(ArtistTopAlbumsAlbumDto::getArtist)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        var qualityArtists = dtoQualityService.validateAgainstBlacklist(artistDtos)
            .stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .toList();

        if (qualityArtists.size() < artistDtos.size()) {
            log.info("Filtered out {} blacklisted artists from albums", 
                artistDtos.size() - qualityArtists.size());
        }

        return qualityArtists;
    }

    /**
     * Filters albums to only include those with valid artists
     */
    private List<ArtistTopAlbumsAlbumDto> filterAlbumsWithValidArtists(
        List<ArtistTopAlbumsAlbumDto> albums, 
        Map<String, LastfmArtist> artistsByName
    ) {
        return albums.stream()
            .filter(album -> album.getArtist() != null && 
                           artistsByName.containsKey(album.getArtist().getName()))
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
        var result = dtoProcessingService.process(
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
        var result = dtoProcessingService.process(
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
            
            // Get artist by name
            if (dto.getArtist() != null && dto.getArtist().getName() != null) {
                LastfmArtist artist = artistsByName.get(dto.getArtist().getName());
                if (artist == null) {
                    log.warn("Artist not found for album: {}", album.getName());
                    return;
                }

                // Create relation
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
}
