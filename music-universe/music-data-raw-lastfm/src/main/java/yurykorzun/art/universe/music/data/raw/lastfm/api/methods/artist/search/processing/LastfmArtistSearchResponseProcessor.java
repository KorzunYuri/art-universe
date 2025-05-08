package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.dto.ArtistSearchArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.dto.ArtistSearchDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.common.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LastfmArtistSearchResponseProcessor extends LastfmApiResponseProcessor<ArtistSearchDtoRoot> {

    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final LastfmArtistService artistService;
    private final EntityFactory<LastfmArtist, ArtistSearchArtistDto> artistFactory;

    @Value("${lastfm.client.methods.artist.search.artistSimilarityThreshold}")
    private double artistSimilarityThreshold;

    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistSearchArtistDto>> artistAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmArtist, ArtistSearchArtistDto> factory =
            new EntityAttributeHandlerFactory<>(LastfmArtist.class, ArtistSearchArtistDto.class);
        artistAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.MBID,  false, "mbid"),
            factory.createHandler(LastfmAttribute.URL, false, "url"),
            factory.createHandler(LastfmAttribute.LISTENERS_COUNT, false, "listenersCount"),
            factory.createHandler(LastfmAttribute.IS_STREAMABLE,  false, "isStreamable", (dto) -> 1 == dto.getStreamable())
        );
    }

    protected LastfmArtistSearchResponseProcessor(
        LastfmApiDtoProcessingService dtoProcessingService, LastfmArtistService artistService,
        EntityFactory<LastfmArtist, ArtistSearchArtistDto> artistFactory
    ) {
        super(ArtistSearchDtoRoot.class);

        this.dtoProcessingService = dtoProcessingService;
        this.artistService = artistService;
        this.artistFactory = artistFactory;
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_SEARCH;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {
        ArtistSearchDtoRoot dtoRoot = parseResponse(sourceApiResponse);

        updateArtists(dtoRoot, sourceApiResponse);
    }

    private void updateArtists(ArtistSearchDtoRoot dtoRoot, LastfmApiResponse sourceApiResponse) {
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();
        String searchString = sourceApiCall.getParams().get(LastfmApiConstants.PARAM_NAME_ARTIST);
        List<ArtistSearchArtistDto> dtos = filterArtistsForSaving(dtoRoot.getRootObject().getMatches().getArtists(), searchString);
        List<String> names = dtos.stream().map(ArtistDto::getName).toList();
        List<LastfmArtist> existingEntities = artistService.findAllByNames(names);

        LastfmApiDtoProcessingResult<LastfmArtist> result = dtoProcessingService.processDtosWithoutRelations(
            dtos, existingEntities, sourceApiResponse,
            artistFactory,
            artistAttrHandlers,
            artistService::saveArtists
        );

        log.info("Saved {} found artists", result.savedEntities().size());
        log.info("Saved {} found artists' attributes", result.savedAttributeValues().size());
    }

    private List<ArtistSearchArtistDto> filterArtistsForSaving(List<ArtistSearchArtistDto> artistDtos, String searchString) {
        List<ArtistSearchArtistDto> list = new ArrayList<>();
        for (ArtistSearchArtistDto dto : artistDtos) {
            if (StringUtils.getSimilarity(dto.getName(), searchString) > artistSimilarityThreshold) {
                list.add(dto);
            }
        }
        return list;
    }
}
