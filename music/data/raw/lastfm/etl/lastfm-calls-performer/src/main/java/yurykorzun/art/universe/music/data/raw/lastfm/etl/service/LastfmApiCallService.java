package yurykorzun.art.universe.music.data.raw.lastfm.etl.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;

import java.util.List;

public interface LastfmApiCallService {

    List<LastfmApiCall> findAllUnprocessedUnexpired();

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateApiCallStatus(LastfmApiCall call, ApiCallStatus status);
}
