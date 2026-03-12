package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyApiResponseRepository;

@Service
@Slf4j
public class SpotifyApiResponseServiceImpl implements SpotifyApiResponseService {

    private final SpotifyApiResponseRepository apiResponseRepository;

    public SpotifyApiResponseServiceImpl(SpotifyApiResponseRepository apiResponseRepository) {
        this.apiResponseRepository = apiResponseRepository;
    }

    @Override
    @Transactional
    public long createResponse(SpotifyApiCall call, String responseBody) {
        SpotifyApiResponse response = SpotifyApiResponse.builder()
            .apiCall(call)
            .responseBody(responseBody)
            .build();
        response = apiResponseRepository.save(response);
        log.debug("Created api_response {} for api_call {}", response.getId(), call.getId());
        return response.getId();
    }
}
