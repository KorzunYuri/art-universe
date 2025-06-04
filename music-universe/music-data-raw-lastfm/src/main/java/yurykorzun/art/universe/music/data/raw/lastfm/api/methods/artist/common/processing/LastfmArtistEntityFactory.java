package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;

public class LastfmArtistEntityFactory<D extends ArtistDto> implements EntityFactory<LastfmArtist, D> {

    @Override
    public LastfmArtist fromDto(D dto, LastfmApiResponse response) {
        // set mandatory attrs
        LastfmArtist.LastfmArtistBuilder<?,?> builder = LastfmArtist.builder()
            .name(dto.getName())
            .url(dto.getUrl())
            .apiCall(response.getApiCall());
        // set optional attrs
        if (dto.getMbid() != null) {
            builder.mbid(dto.getMbid());
        }
        // set extended attrs
        return setExtensionFields(builder, dto).build();
    }

    protected LastfmArtist.LastfmArtistBuilder<?,?> setExtensionFields(LastfmArtist.LastfmArtistBuilder<?,?> builder, D dto) {
        return builder;
    }

    @Override
    public LastfmArtist clone(LastfmArtist entity) {
        return LastfmArtist.builder()
                .id(entity.getId())
                .name(entity.getName())
                .url(entity.getUrl())
                .mbid(entity.getMbid())
                .apiCall(entity.getApiCall())
                .approvalStatus(entity.getApprovalStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
