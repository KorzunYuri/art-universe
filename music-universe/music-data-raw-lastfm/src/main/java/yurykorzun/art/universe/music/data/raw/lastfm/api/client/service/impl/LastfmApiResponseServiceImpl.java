package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;

@Service
public class LastfmApiResponseServiceImpl implements LastfmApiResponseService {

    private final LastfmApiResponseRepository repository;

    public LastfmApiResponseServiceImpl(LastfmApiResponseRepository repository) {
        this.repository = repository;
    }

    @Override
    public long create(LastfmApiResponseCreateRequest dto) {
        LastfmApiResponse response = repository.save(dtoToApiResponse(dto));
        return response.getId();
    }

    @Override
    public void setStatus(long id, ApiResponseStatus status) throws IllegalStateException {
        LastfmApiResponse response = repository.getReferenceById(id);
        response.setStatus(status);
        repository.save(response);
    }

    private static LastfmApiResponse dtoToApiResponse(LastfmApiResponseCreateRequest dto) {
        return LastfmApiResponse.builder()
                .apiCallId(dto.getApiCallId())
                .apiCallType(dto.getApiCallType())
                .responseBody(dto.getResponseBody())
            .build();
    }

}
