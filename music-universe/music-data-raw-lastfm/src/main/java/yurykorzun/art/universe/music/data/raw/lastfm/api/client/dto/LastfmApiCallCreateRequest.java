package yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.api.client.dto.ApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;

import java.util.Map;

@SuperBuilder
@Getter
public class LastfmApiCallCreateRequest extends ApiCallCreateRequest {
    private LastfmApiCallType type;
    private Map<String, String> params;
    private long dataSnapshotId;
}
