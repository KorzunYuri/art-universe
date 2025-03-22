package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;

import java.util.List;

public abstract class LastfmApiCallGenerator {

    protected final LastfmApiCallService apiCallService;

    protected LastfmApiCallGenerator(LastfmApiCallService apiCallService) {
        this.apiCallService = apiCallService;
        LastfmApiCallGeneratorsRegistry.register(getType(), this);
    }

    public abstract LastfmApiCallType getType();

    public void createApiCalls() {
        List<LastfmApiCallCreateRequest> apiCallCreationRequests = generateApiCallCreationRequests();
        apiCallService.createApiCalls(apiCallCreationRequests);
    }

    protected abstract List<LastfmApiCallCreateRequest> generateApiCallCreationRequests();

}
