package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;

import java.util.Collection;
import java.util.Queue;

public interface LastfmApiCallPrioritizer {

    Queue<LastfmApiCall> prioritizeApiCalls(Collection<LastfmApiCall> apiCalls);

}
