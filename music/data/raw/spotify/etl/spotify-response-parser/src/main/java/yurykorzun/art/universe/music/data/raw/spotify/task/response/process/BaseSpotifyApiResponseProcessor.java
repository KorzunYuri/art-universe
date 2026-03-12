package yurykorzun.art.universe.music.data.raw.spotify.task.response.process;

import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;

import java.io.IOException;

public abstract class BaseSpotifyApiResponseProcessor {

    protected BaseSpotifyApiResponseProcessor() {
        SpotifyApiResponseProcessorsRegistry.register(getApiCallType(), this.getClass());
    }

    public abstract SpotifyApiCallType getApiCallType();

    /**
     * Processes the API response, writing staged records into the given iteration's staging tables.
     *
     * @return number of records staged
     */
    public abstract int process(SpotifyApiResponse response, StagingIteration iteration) throws IOException;
}
