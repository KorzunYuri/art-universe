package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.track;

import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils.DataQualityUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.TrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;

public class LastfmTrackEntityFactory<D extends TrackDto>  implements EntityFactory<LastfmTrack, D> {

    @Override
    public LastfmTrack fromDto(D dto, LastfmApiCall sourceApiCall) {
        return setExtensionFields(
            LastfmTrack.builder()
                .name(dto.getName())
                .url(DataQualityUtil.normalizeTrackUrl(dto.getUrl()))
                .mbid(dto.getMbid())
                .apiCall(sourceApiCall),
            dto
        ).build();
    }

    protected LastfmTrack.LastfmTrackBuilder<?,?> setExtensionFields(LastfmTrack.LastfmTrackBuilder<?,?> builder, D dto) {
        return builder;
    }

    @Override
    public LastfmTrack clone(LastfmTrack entity) {
        return LastfmTrack.builder()
                .id(entity.getId())
                .name(entity.getName())
                .url(entity.getUrl())
                .mbid(entity.getMbid())
                .duration(entity.getDuration())
                .apiCall(entity.getApiCall())
                .approvalStatus(entity.getApprovalStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
