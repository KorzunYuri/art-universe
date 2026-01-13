package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.track.getinfo;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.LastfmAlbumEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.getinfo.TrackGetInfoAlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;

@Component
public class LastfmTrackGetInfoAlbumFactory extends LastfmAlbumEntityFactory<TrackGetInfoAlbumDto> {

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
