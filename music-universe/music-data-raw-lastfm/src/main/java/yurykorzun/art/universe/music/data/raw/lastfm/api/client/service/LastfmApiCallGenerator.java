package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;

public abstract class LastfmApiCallGenerator {

    protected LastfmApiCallGenerator() {
        LastfmApiCallGeneratorsRegistry.register(getApiCallType(), this);
    }

    public abstract LastfmApiCallType getApiCallType();

    public abstract void createApiCalls();

}
