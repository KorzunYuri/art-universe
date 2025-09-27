package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.processing;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMapping;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.dedup.ArtistDeduplicationUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksTrackArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.LastfmArtistTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmTrackService;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmTagTopTracksResponseProcessor extends LastfmApiResponseProcessor<TagTopTracksDtoRoot> {

    private final LastfmTagService tagService;
    private final LastfmArtistService artistService;
    private final LastfmTrackService trackService;
    private final LastfmArtistTrackService artistTrackService;
    private final DtoQualityService dtoQualityService;
    private final EntityFactory<LastfmArtist, TagTopTracksTrackArtistDto> artistFactory;
    private final LastfmApiDtoProcessingService dtoProcessingService;

    private final Set<TagTopTracksDtoRoot> rootsWithMissingAttrsLogged = new HashSet<>();

    protected LastfmTagTopTracksResponseProcessor(
        LastfmTagService tagService,
        LastfmArtistService artistService,
        LastfmTrackService trackService,
        LastfmArtistTrackService artistTrackService,
        LastfmApiDtoProcessingService dtoProcessingService,
        DtoQualityService dtoQualityService,
        EntityFactory<LastfmArtist, TagTopTracksTrackArtistDto> artistFactory
    ) {
        super(TagTopTracksDtoRoot.class);

        this.artistService = artistService;
        this.trackService = trackService;
        this.tagService = tagService;
        this.artistTrackService = artistTrackService;
        this.dtoQualityService = dtoQualityService;
        this.artistFactory = artistFactory;
        this.dtoProcessingService = dtoProcessingService;
    }

    private static final List<EntityAttributeHandler<LastfmTrack, ?, TagTopTracksTrackDto>> trackAttrHandlers;

    private static final List<EntityAttributeHandler<LastfmArtist, ?, TagTopTracksTrackArtistDto>> artistAttrHandlers;
    
    static {
        EntityAttributeHandlerFactory<LastfmTrack, TagTopTracksTrackDto> trackAttrHandlerFactory =
            new EntityAttributeHandlerFactory<>(LastfmTrack.class, TagTopTracksTrackDto.class);
        trackAttrHandlers = List.of(
            trackAttrHandlerFactory.createHandler(LastfmAttribute.MBID,  false, "mbid"),
            trackAttrHandlerFactory.createHandler(LastfmAttribute.URL, false, "url"),
            trackAttrHandlerFactory.createHandler(LastfmAttribute.DURATION, false, "duration")
        );

        EntityAttributeHandlerFactory<LastfmArtist, TagTopTracksTrackArtistDto> factory =
            new EntityAttributeHandlerFactory<>(LastfmArtist.class, TagTopTracksTrackArtistDto.class);
        artistAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.MBID,  false, "mbid"),
            factory.createHandler(LastfmAttribute.URL, false, "url"));
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_TRACKS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {

        TagTopTracksDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();
        LastfmTag sourceTag = tagService.findById(sourceApiCall.getEntityId())
            .orElseThrow(() -> new EntityNotFoundException(String.format("Source tag with ID=%s not found", sourceApiCall.getEntityId())));

        // First validate and save artists from ALL tracks (no filtering by track validity)
        var artistMappingResult = updateArtists(dtoRoot, sourceApiCall);

        // Then validate and save tracks using validated artists
        var trackMappingResult = updateTracks(dtoRoot, sourceApiCall, artistMappingResult);

        // Finally, bind all tracks to artists
        updateArtistTrackRelations(trackMappingResult, artistMappingResult, sourceApiCall);

        // Clean cache
        rootsWithMissingAttrsLogged.remove(dtoRoot);
    }

    private LastfmApiDtoProcessingResult<LastfmArtist, TagTopTracksTrackArtistDto> updateArtists(TagTopTracksDtoRoot dtoRoot, LastfmApiCall sourceApiCall) {

        // Extract ALL artists from tracks (no filtering by track validity)
        List<TagTopTracksTrackArtistDto> allArtistDtos = dtoRoot.getRootObject().getTracks().stream()
            .map(TagTopTracksTrackDto::getArtist)
            .filter(Objects::nonNull)
            .toList();
        allArtistDtos = ArtistDeduplicationUtils.deduplicateArtistDtos(allArtistDtos);

        // Validate artists against blacklist
        var qualityArtistDtos = dtoQualityService.validateAgainstBlacklist(allArtistDtos)
            .stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .toList();

        if (qualityArtistDtos.size() < allArtistDtos.size()) {
            log.info("Filtered out {} blacklisted artists from tag's top tracks", 
                allArtistDtos.size() - qualityArtistDtos.size());
        }

        var result = dtoProcessingService.process(
            sourceApiCall,
            qualityArtistDtos,
            artistFactory,
            artistAttrHandlers,
            artistService
        );
        log.info("saved {} tag's tracks' artists", result.actualEntities().size());
        log.info("saved {} tag's tracks' artists' attributes", result.savedAttributeRecordsCount());

        return result;
    }

    private LastfmApiDtoProcessingResult<LastfmTrack, TagTopTracksTrackDto> updateTracks(
        TagTopTracksDtoRoot dtoRoot, 
        LastfmApiCall sourceApiCall,
        LastfmApiDtoProcessingResult<LastfmArtist, TagTopTracksTrackArtistDto> artistMappingResult
    ) {
        // Get all valid tracks (with URL and artist)
        List<TagTopTracksTrackDto> allValidTrackDtos = getValidTrackDtos(dtoRoot);

        // Filter tracks to only include those with valid (non-blacklisted) artists
        List<TagTopTracksTrackDto> tracksWithValidArtists = allValidTrackDtos.stream()
            .filter(track -> track.getArtist() != null && 
                           artistMappingResult.entityMapping().getMap().containsKey(track.getArtist()))
            .toList();

        // Validate tracks against blacklist
        var qualityTrackDtos = dtoQualityService.validateAgainstBlacklist(tracksWithValidArtists)
            .stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .toList();

        if (qualityTrackDtos.size() < allValidTrackDtos.size()) {
            log.info("Filtered {} tracks: {} due to blacklisted artists, {} due to blacklisted tracks",
                allValidTrackDtos.size() - qualityTrackDtos.size(),
                allValidTrackDtos.size() - tracksWithValidArtists.size(),
                tracksWithValidArtists.size() - qualityTrackDtos.size());
        }

        // Create track factory with artists from the previous step
        LastfmTagTopTracksTrackFactory trackFactory = new LastfmTagTopTracksTrackFactory(artistMappingResult);

        var result = dtoProcessingService.process(
            sourceApiCall,
            qualityTrackDtos,
            trackFactory,
            trackAttrHandlers,
            trackService
        );
        log.info("saved {} tag's tracks", result.actualEntities().size());
        log.info("saved {} tag's tracks' attributes", result.savedAttributeRecordsCount());

        return result;
    }

    private void updateArtistTrackRelations(
        LastfmApiDtoProcessingResult<LastfmTrack, TagTopTracksTrackDto> trackMappingResult,
        LastfmApiDtoProcessingResult<LastfmArtist, TagTopTracksTrackArtistDto> artistMappingResult,
        LastfmApiCall sourceApiCall
    ) {
        List<LastfmArtistTrack> relations = trackMappingResult.entityMapping().values().stream()
            .map(trackMapping -> {
                var artistDto = trackMapping.getDto().getArtist();
                EntityMapping<LastfmArtist, TagTopTracksTrackArtistDto> artistMapping =
                    artistMappingResult.entityMapping().get(artistDto);
                if (artistMapping == null) {
                    log.warn("Artist not found for track {} - {}", artistDto.getName(), trackMapping.getDto().getName());
                    return null;
                }
                LastfmArtist artist = artistMapping.getNewEntity();
                if (artist == null) {
                    log.warn("Artist {} wasn't saved", artistDto);
                    return null;
                }
                return LastfmArtistTrack.builder()
                        .apiCall(sourceApiCall)
                        .artist(artist)
                        .track(trackMapping.getNewEntity())
                    .build();
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        artistTrackService.upsertAll(relations);
        log.info("saved {} artist-track relations", relations.size());
    }

    /**
     * Gets valid track DTOs (with URL and artist) and logs missing attributes
     */
    private List<TagTopTracksTrackDto> getValidTrackDtos(TagTopTracksDtoRoot dtoRoot) {
        return dtoRoot.getRootObject().getTracks().stream()
                .peek(logMissingAttributes(dtoRoot))
                .filter(t -> t.getUrl() != null)
            .toList();
    }

    private Consumer<TagTopTracksTrackDto> logMissingAttributes(final TagTopTracksDtoRoot dtoRoot) {
        return (dto) -> {
            if (!rootsWithMissingAttrsLogged.contains(dtoRoot)) {
                rootsWithMissingAttrsLogged.add(dtoRoot);
                if (dto.getUrl() == null) {
                    log.error("Url is null in track {}:{} by artist {}", dto.getName(), dto.getMbid(), dto.getArtist());
                }
                if (dto.getArtist() == null) {
                    log.warn("Artist is null in track {}:{}:{}", dto.getName(), dto.getMbid(), dto.getUrl());
                }
            }
        };
    }
}
