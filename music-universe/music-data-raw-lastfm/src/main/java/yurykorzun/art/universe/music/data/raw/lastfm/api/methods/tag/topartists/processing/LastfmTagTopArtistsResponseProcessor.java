package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.processing.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultEntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.TagTopArtistsArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.TagTopArtistsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class LastfmTagTopArtistsResponseProcessor extends LastfmApiResponseProcessor<TagTopArtistsDtoRoot> {

    private final LastfmArtistService artistService;
    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final LastfmArtistEntityFactory<TagTopArtistsArtistDto> artistFactory;

    protected LastfmTagTopArtistsResponseProcessor(
        LastfmArtistService artistService,
        LastfmApiDtoProcessingService dtoProcessingService
    ) {
        super(TagTopArtistsDtoRoot.class);

        this.artistService = artistService;
        this.dtoProcessingService = dtoProcessingService;

        this.artistFactory = new LastfmArtistEntityFactory<>();
    }

    private static final List<EntityAttributeHandler<LastfmArtist, ?, TagTopArtistsArtistDto>> attrHandlers = List.of(
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.MBID,  false,
            LastfmArtist::getMbid, LastfmArtist::setMbid, TagTopArtistsArtistDto::getMbid),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.URL, false,
            LastfmArtist::getUrl, LastfmArtist::setUrl, TagTopArtistsArtistDto::getUrl),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.IS_STREAMABLE,  false,
            LastfmArtist::getIsStreamable, LastfmArtist::setIsStreamable,
            (dto) -> 1 == dto.getStreamable()),
        DefaultEntityAttributeHandler.forExternalAttribute(LastfmAttribute.RANK,  true,
            (dto) -> dto.getRecordInfo().getRank())
    );

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_ARTISTS;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processResponse(LastfmApiResponse response) throws IOException {

        TagTopArtistsDtoRoot dtoRoot = parseResponse(response);
        List<TagTopArtistsArtistDto> dtos = dtoRoot.getTopArtists().getArtists();

        List<String> artistNames = dtos.stream().map(ArtistDto::getName).toList();
        List<LastfmArtist> existingArtists = artistService.findAllByNames(artistNames);

        LastfmApiDtoProcessingResult<LastfmArtist> result = dtoProcessingService.processDtosWithRelations(
            dtos, existingArtists, response,
            artistFactory, attrHandlers, artistService::saveArtists
        );
        log.info("saved {} tag's artists", result.savedEntities().size());
        log.info("saved {} tag's artists' attributes", result.savedAttributeValues().size());
        log.info("saved {} tag-artist relations", result.savedEntityRelations().size());
    }

}
