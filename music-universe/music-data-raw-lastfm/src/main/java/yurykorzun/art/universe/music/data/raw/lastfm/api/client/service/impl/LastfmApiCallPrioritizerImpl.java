package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallPrioritizer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

@Component
public class LastfmApiCallPrioritizerImpl implements LastfmApiCallPrioritizer {

    @Override
    public Queue<LastfmApiCall> prioritizeApiCalls(Collection<LastfmApiCall> apiCalls) {
        return new LinkedList<>(apiCalls);
    }
}
