package yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.data.raw.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;

import java.util.List;

@Service
public class LastfmApiCallServiceImpl implements LastfmApiCallService {

    private final LastfmApiCallRepository apiCallRepository;

    public LastfmApiCallServiceImpl(LastfmApiCallRepository apiCallRepository) {
        this.apiCallRepository = apiCallRepository;
    }

    @Override
    public List<LastfmApiCall> findAllUnprocessedUnexpired() {
        return apiCallRepository.findAllUnprocessedUnexpired();
    }

    @Override
    public void updateApiCallStatus(LastfmApiCall call, ApiCallStatus status) {
        call.setStatus(status);
        apiCallRepository.save(call);
    }

}
