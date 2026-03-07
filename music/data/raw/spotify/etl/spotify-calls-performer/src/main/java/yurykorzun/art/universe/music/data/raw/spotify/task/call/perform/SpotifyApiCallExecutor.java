package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform;

import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;

public interface SpotifyApiCallExecutor {

    void execute(SpotifyApiCall call);
}
