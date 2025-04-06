package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto.TrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

public class LastfmTrackEntityFactory implements EntityFactory<LastfmTrack, TrackDto> {

    @Override
    public LastfmTrack fromDto(TrackDto dto, LastfmApiResponse response) {
        return LastfmTrack.builder()
                .name(dto.getName())
                .url(dto.getUrl())
                .mbid(dto.getMbid())
                .streamable(dto.getStreamableObject().getFullTrack())
                .duration(dto.getDuration())
                .apiCall(response.getApiCall())
            .build();
    }

    @Override
    public LastfmTrack clone(LastfmTrack entity) {
        return LastfmTrack.builder()
                .id(entity.getId())
                .name(entity.getName())
                .url(entity.getUrl())
                .mbid(entity.getMbid())
                .streamable(entity.getStreamable())
                .duration(entity.getDuration())
                .apiCall(entity.getApiCall())
                .approvalStatus(entity.getApprovalStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
