package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory;

import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.AlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;

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
