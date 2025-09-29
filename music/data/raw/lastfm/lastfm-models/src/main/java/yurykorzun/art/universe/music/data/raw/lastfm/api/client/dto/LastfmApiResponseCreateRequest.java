package yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.api.client.dto.ApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;

@SuperBuilder
@Getter
public class LastfmApiResponseCreateRequest extends ApiResponseCreateRequest {
    private LastfmApiCall apiCall;
}
