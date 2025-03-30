package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.ArtistsRankedDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;

public class LastfmArtistEntityFactory extends EntityFactory<LastfmArtist, ArtistsRankedDto> {

    @Override
    protected LastfmArtist fromDto(ArtistsRankedDto dto, LastfmApiResponse response) {
        return LastfmArtist.builder()
                .name(dto.getName())
                .url(dto.getUrl())
                .mbid(dto.getMbid())
                .apiCall(response.getApiCall())
            .build();
    }

    @Override
    protected LastfmArtist clone(LastfmArtist entity) {
        return LastfmArtist.builder()
                .id(entity.getId())
                .name(entity.getName())
                .url(entity.getUrl())
                .mbid(entity.getMbid())
                .apiCall(entity.getApiCall())
            .build();
    }
}
