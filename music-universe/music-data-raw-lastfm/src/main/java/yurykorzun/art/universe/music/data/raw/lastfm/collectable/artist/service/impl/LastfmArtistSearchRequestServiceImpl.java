package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtistSearchRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistSearchRequestRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistSearchRequestService;

import java.util.List;

@Service
public class LastfmArtistSearchRequestServiceImpl implements LastfmArtistSearchRequestService {

    private final LastfmArtistSearchRequestRepository searchRequestRepository;

    public LastfmArtistSearchRequestServiceImpl(LastfmArtistSearchRequestRepository searchRequestRepository) {
        this.searchRequestRepository = searchRequestRepository;
    }

    @Override
    public List<LastfmArtistSearchRequest> findUnprocessed(int batchLimit) {
        return searchRequestRepository.findUnprocessed(batchLimit);
    }

    @Override
    public List<LastfmArtistSearchRequest> saveRequests(List<LastfmArtistSearchRequest> searchRequests) {
        return searchRequestRepository.saveAll(searchRequests);
    }
}
