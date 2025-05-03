package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.dto.AlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.dto.ArtistTopAlbumsAlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.dto.ArtistTopAlbumsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultEntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class LastfmArtistTopAlbumsResponseProcessor extends LastfmApiResponseProcessor<ArtistTopAlbumsDtoRoot> {

    private final LastfmAlbumService albumService;
    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final EntityFactory<LastfmAlbum, ArtistTopAlbumsAlbumDto> albumEntityFactory;

    @Value("${lastfm.client.methods.artist.topAlbums.albumPlayCountThreshold:10000}")
    private int albumPlayCountThreshold;

    private static final List<EntityAttributeHandler<LastfmAlbum, ?, ArtistTopAlbumsAlbumDto>> albumAttrHandlers = List.of(
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.URL, false,
            LastfmAlbum::getUrl, LastfmAlbum::setUrl, ArtistTopAlbumsAlbumDto::getUrl),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.MBID, false,
            LastfmAlbum::getMbid, LastfmAlbum::setMbid, ArtistTopAlbumsAlbumDto::getMbid),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.PLAY_COUNT, false,
            LastfmAlbum::getPlayCount, LastfmAlbum::setPlayCount, ArtistTopAlbumsAlbumDto::getPlayCount)
    );

    protected LastfmArtistTopAlbumsResponseProcessor(
        LastfmAlbumService albumService,
        LastfmApiDtoProcessingService dtoProcessingService,
        EntityFactory<LastfmAlbum, ArtistTopAlbumsAlbumDto> albumEntityFactory
    ) {
        super(ArtistTopAlbumsDtoRoot.class);

        this.albumService = albumService;
        this.dtoProcessingService = dtoProcessingService;
        this.albumEntityFactory = albumEntityFactory;
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_ALBUMS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {

        ArtistTopAlbumsDtoRoot dtoRoot = parseResponse(sourceApiResponse);

        updateAlbums(sourceApiResponse, dtoRoot);
    }

    private void updateAlbums(LastfmApiResponse sourceApiResponse, ArtistTopAlbumsDtoRoot dtoRoot) {
        List<ArtistTopAlbumsAlbumDto> dtos = getAlbumsToSave(dtoRoot);
        List<String> urls = dtos.stream().map(AlbumDto::getUrl).toList();
        List<LastfmAlbum> existingEntities = albumService.findAllByUrls(urls);

        LastfmApiDtoProcessingResult<LastfmAlbum> result = dtoProcessingService.processDtosWithRelations(
            dtos, existingEntities, sourceApiResponse,
            albumEntityFactory,
            albumAttrHandlers,
            albumService::saveAlbums
        );
        log.info("Saved {} artist's albums", result.savedEntities().size());
        log.info("Saved {} artist's albums' attributes", result.savedAttributeValues().size());
        log.info("Saved {} artist-album relations", result.savedEntityRelations().size());
    }

    private List<ArtistTopAlbumsAlbumDto> getAlbumsToSave(ArtistTopAlbumsDtoRoot dtoRoot) {
        return dtoRoot.getTopAlbumsObject().getAlbums().stream()
            .filter(dto -> dto.getPlayCount() >= albumPlayCountThreshold)
            .toList();
    }
}
