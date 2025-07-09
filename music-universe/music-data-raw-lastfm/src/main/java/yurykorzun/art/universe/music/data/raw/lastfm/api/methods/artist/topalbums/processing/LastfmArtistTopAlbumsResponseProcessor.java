package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.processing;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
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
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmArtistTopAlbumsResponseProcessor extends LastfmApiResponseProcessor<ArtistTopAlbumsDtoRoot> {

    private final LastfmArtistService artistService;
    private final LastfmAlbumService albumService;
    private final LastfmArtistAlbumService artistAlbumService;
    private final EntityFactory<LastfmAlbum, ArtistTopAlbumsAlbumDto> albumEntityFactory;
    private final LastfmApiDtoProcessingService dtoProcessingService;

    @Value("${lastfm.client.methods.artist.topAlbums.albumPlayCountThreshold:10000}")
    private int albumPlayCountThreshold;

    private static final List<EntityAttributeHandler<LastfmAlbum, ?, ArtistTopAlbumsAlbumDto>> albumAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmAlbum, ArtistTopAlbumsAlbumDto> factory = 
            new EntityAttributeHandlerFactory<>(LastfmAlbum.class, ArtistTopAlbumsAlbumDto.class);
        albumAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.URL, false, "url"),
            factory.createHandler(LastfmAttribute.MBID, false, "mbid"),
            factory.createHandler(LastfmAttribute.PLAY_COUNT, false, "playCount")
        );
    }

    protected LastfmArtistTopAlbumsResponseProcessor(
        LastfmArtistService artistService,
        LastfmAlbumService albumService,
        LastfmArtistAlbumService artistAlbumService,
        LastfmApiDtoProcessingService dtoProcessingService,
        EntityFactory<LastfmAlbum, ArtistTopAlbumsAlbumDto> albumEntityFactory
    ) {
        super(ArtistTopAlbumsDtoRoot.class);

        this.artistService = artistService;
        this.albumService = albumService;
        this.artistAlbumService = artistAlbumService;
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
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();
        LastfmArtist sourceArtist = artistService.findById(sourceApiCall.getEntityId())
            .orElseThrow(() -> new EntityNotFoundException(String.format("Source artist with ID=%s not found", sourceApiCall.getEntityId())));

        var albumsMappingResult = updateAlbums(dtoRoot, sourceApiCall, sourceArtist);

        bindAlbumsToArtist(albumsMappingResult, sourceApiCall, sourceArtist);
    }

    private LastfmApiDtoProcessingResult<LastfmAlbum, ArtistTopAlbumsAlbumDto> updateAlbums(ArtistTopAlbumsDtoRoot dtoRoot, LastfmApiCall sourceApiCall, LastfmArtist sourceArtist) {
        List<ArtistTopAlbumsAlbumDto> dtos = getAlbumsToSave(dtoRoot, sourceArtist);

        LastfmApiDtoProcessingResult<LastfmAlbum, ArtistTopAlbumsAlbumDto> result = dtoProcessingService.process(
            sourceApiCall,
            dtos,
            albumEntityFactory,
            albumAttrHandlers,
            albumService
        );
        log.info("Saved {} artist's albums", result.savedEntities().size());
        log.info("Saved {} artist's albums' attributes", result.savedAttributeValues().size());

        return result;
    }

    private List<ArtistTopAlbumsAlbumDto> getAlbumsToSave(ArtistTopAlbumsDtoRoot dtoRoot, LastfmArtist sourceArtist) {
        return dtoRoot.getTopAlbumsObject().getAlbums().stream()
            .filter(albumFilter(sourceArtist))
            .toList();
    }

    private Predicate<ArtistTopAlbumsAlbumDto> albumFilter(LastfmArtist sourceArtist) {
        return dto -> {
            if (dto.getPlayCount() < albumPlayCountThreshold) {
                return false;
            }

            if (!sourceArtist.getName().equals(dto.getArtist().getName())) {
                log.warn("Artist name in album doesn't match with the on in the album, album: {}, source artist: {}, album artist: {}",
                    dto.getName(), sourceArtist.getName(), dto.getArtist().getName());
                return false;
            }

            return true;
        };
    }

    private void bindAlbumsToArtist(
        LastfmApiDtoProcessingResult<LastfmAlbum, ArtistTopAlbumsAlbumDto> albumsMappingResult,
        LastfmApiCall sourceApiCall,
        LastfmArtist artist
    ) {
        List<LastfmArtistAlbum> relations = albumsMappingResult.savedEntities().stream()
            .map((album) -> LastfmArtistAlbum.builder()
                    .apiCall(sourceApiCall)
                    .artist(artist)
                    .album(album)
                .build())
            .collect(Collectors.toList());
        artistAlbumService.upsertAll(relations);
        log.info("saved {} artist-album relations", relations.size());
    }
}
