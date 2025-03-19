package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;

import java.util.List;

public abstract class LastfmApiCallGenerator {

    protected LastfmApiCallGenerator() {
        LastfmApiCallGeneratorsRegistry.register(getType(), this);
    }

    public abstract LastfmApiCallType getType();
    public abstract List<LastfmApiCallCreateRequest> generateApiCalls();

}
