package yurykorzun.art.universe.music.data.raw.spotify.task.call.generate;

import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;

public abstract class BaseSpotifyApiCallGenerator {

    protected BaseSpotifyApiCallGenerator() {
        SpotifyApiCallGeneratorsRegistry.register(getApiCallType(), this.getClass());
    }

    public abstract SpotifyApiCallType getApiCallType();

    public abstract void createApiCalls();
}
