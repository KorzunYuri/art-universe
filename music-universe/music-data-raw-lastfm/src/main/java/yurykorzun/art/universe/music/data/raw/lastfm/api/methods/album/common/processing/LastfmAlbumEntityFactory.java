package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.dto.AlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;

public class LastfmAlbumEntityFactory<D extends AlbumDto> implements EntityFactory<LastfmAlbum, D> {

    @Override
    public LastfmAlbum fromDto(D dto, LastfmApiCall sourceApiCall) {
        // set mandatory attrs
        LastfmAlbum.LastfmAlbumBuilder<?, ?> builder = LastfmAlbum.builder()
            .apiCall(sourceApiCall)
            .name(dto.getName())
            .url(dto.getUrl())
        ;
        // set optional fields
        if (dto.getMbid() != null) {
            builder.mbid(dto.getMbid());
        }
        // set extended attrs
        return setExtensionFields(builder, dto).build();
    }

    protected LastfmAlbum.LastfmAlbumBuilder<?,?> setExtensionFields(LastfmAlbum.LastfmAlbumBuilder<?,?> builder, D dto) {
        return builder;
    }

    @Override
    public LastfmAlbum clone(LastfmAlbum entity) {
        return LastfmAlbum.builder()
                .id(entity.getId())
                .apiCall(entity.getApiCall())
                .name(entity.getName())
                .description(entity.getDescription())
                .url(entity.getUrl())
                .mbid(entity.getMbid())
                .playCount(entity.getPlayCount())
                .listenersCount(entity.getListenersCount())
                .publishTs(entity.getPublishTs())
                .artist(entity.getArtist())
            .build();
    }
}
