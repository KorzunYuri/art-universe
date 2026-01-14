package yurykorzun.art.universe.music.data.raw.lastfm.etl.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.data.raw.common.etl.dto.ApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

import java.util.Map;

@SuperBuilder
@Getter
public class LastfmApiCallCreateRequest extends ApiCallCreateRequest {

    @NonNull
    private LastfmApiCallType type;

    @NonNull
    private LastfmEntityType entityType;

    private long entityId;

    @Builder.Default
    private Map<String, String> params = Map.of();

    @NonNull
    private long dataSnapshotId;
}
