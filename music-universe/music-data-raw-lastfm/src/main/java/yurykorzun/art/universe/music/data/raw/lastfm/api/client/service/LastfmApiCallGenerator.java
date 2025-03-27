package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;

public abstract class LastfmApiCallGenerator {

    protected LastfmApiCallGenerator() {
        LastfmApiCallGeneratorsRegistry.register(getType(), this);
    }

    public abstract LastfmApiCallType getType();

    public abstract void createApiCalls();

}
