package yurykorzun.art.universe.music.data.raw.lastfm.etl.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.etl.dto.ApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;

@SuperBuilder
@Getter
public class LastfmApiResponseCreateRequest extends ApiResponseCreateRequest {
    private LastfmApiCall apiCall;
}
