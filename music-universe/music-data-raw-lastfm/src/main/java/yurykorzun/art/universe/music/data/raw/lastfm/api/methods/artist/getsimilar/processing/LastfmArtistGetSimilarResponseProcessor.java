package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.dto.ArtistGetSimilarArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.dto.ArtistGetSimilarDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultEntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class LastfmArtistGetSimilarResponseProcessor extends LastfmApiResponseProcessor<ArtistGetSimilarDtoRoot> {

    private final LastfmArtistService artistService;
    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final EntityFactory<LastfmArtist, ArtistGetSimilarArtistDto> artistFactory;

    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistGetSimilarArtistDto>> artistAttrHandlers = List.of(
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.MBID,  false,
            LastfmArtist::getMbid, LastfmArtist::setMbid, ArtistGetSimilarArtistDto::getMbid),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.URL, false,
            LastfmArtist::getUrl, LastfmArtist::setUrl, ArtistGetSimilarArtistDto::getUrl),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.IS_STREAMABLE,  false,
            LastfmArtist::getIsStreamable, LastfmArtist::setIsStreamable,
            (dto) -> 1 == dto.getStreamable()),
        DefaultEntityAttributeHandler.forExternalAttribute(LastfmAttribute.MATCH_COEFF, true,
            (dto) -> (int) (dto.getMatchCoeff() * 100))
    );

    protected LastfmArtistGetSimilarResponseProcessor(
        LastfmArtistService artistService,
        LastfmApiDtoProcessingService dtoProcessingService,
        EntityFactory<LastfmArtist, ArtistGetSimilarArtistDto> artistFactory
    ) {
        super(ArtistGetSimilarDtoRoot.class);

        this.artistService = artistService;
        this.dtoProcessingService = dtoProcessingService;
        this.artistFactory = artistFactory;
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_GET_SIMILAR;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {
        ArtistGetSimilarDtoRoot dtoRoot = parseResponse(sourceApiResponse);

        updateSimilarArtists(dtoRoot, sourceApiResponse);
    }

    private void updateSimilarArtists(ArtistGetSimilarDtoRoot dtoRoot, LastfmApiResponse sourceApiResponse) {
        List<ArtistGetSimilarArtistDto> dtos = filterDtosForSaving(dtoRoot);
        List<String> artistNames = dtos.stream().map(ArtistGetSimilarArtistDto::getName).toList();
        List<LastfmArtist> existingEntities = artistService.findAllByNames(artistNames);

        LastfmApiDtoProcessingResult<LastfmArtist> result = dtoProcessingService.processDtosWithoutRelations(
            dtos, existingEntities, sourceApiResponse,
            artistFactory,
            artistAttrHandlers,
            artistService::saveArtists
        );
        log.info("Saved {} similar artists", result.savedEntities().size());
        log.info("Saved {} similar artists' attributes", result.savedAttributeValues().size());
    }

    /**
     * Returns DTOs of artists for saving, filtered. Currently, no filter is implemented, so all artists will be saved.
     * TODO consider filtering out similar artists by match coeff
     */
    private List<ArtistGetSimilarArtistDto> filterDtosForSaving(ArtistGetSimilarDtoRoot dtoRoot) {
        return dtoRoot.getRootObject().getArtists();
    }
}
