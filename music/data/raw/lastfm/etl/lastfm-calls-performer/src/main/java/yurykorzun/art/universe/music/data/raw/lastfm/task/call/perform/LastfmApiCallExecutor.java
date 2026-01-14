package yurykorzun.art.universe.music.data.raw.lastfm.task.call.perform;

import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;

public interface LastfmApiCallExecutor {

    void execute(LastfmApiCall call);

}
