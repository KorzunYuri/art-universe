package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtistSearchRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmArtistSearchRequestRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistSearchRequestService;

@Service
public class LastfmArtistSearchRequestServiceImpl implements LastfmArtistSearchRequestService {

    private final LastfmArtistSearchRequestRepository searchRequestRepository;

    public LastfmArtistSearchRequestServiceImpl(LastfmArtistSearchRequestRepository searchRequestRepository) {
        this.searchRequestRepository = searchRequestRepository;
    }

    @Override
    public LastfmArtistSearchRequest saveRequest(String searchString) {
        LastfmArtistSearchRequest request = LastfmArtistSearchRequest.builder()
            .searchString(searchString)
            .processed(false)
            .build();
        return searchRequestRepository.save(request);
    }
}
