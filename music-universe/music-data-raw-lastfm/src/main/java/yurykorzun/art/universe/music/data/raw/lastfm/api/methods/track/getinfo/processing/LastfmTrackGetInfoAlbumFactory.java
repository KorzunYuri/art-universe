package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.processing;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.processing.LastfmAlbumEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.dto.TrackGetInfoAlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;

@Component
class LastfmTrackGetInfoAlbumFactory extends LastfmAlbumEntityFactory<TrackGetInfoAlbumDto> {

    @Override
    public LastfmAlbum fromDto(TrackGetInfoAlbumDto dto, LastfmApiCall sourceApiCall) {
        return LastfmAlbum.builder()
            .name(dto.getName())
            .mbid(dto.getMbid())
            .url(dto.getUrl())
            .apiCall(sourceApiCall)
            .build();
    }
}
