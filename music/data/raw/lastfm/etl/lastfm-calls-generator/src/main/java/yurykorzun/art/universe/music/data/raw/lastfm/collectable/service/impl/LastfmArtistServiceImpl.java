package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistService;

import java.util.List;

@Service
public class LastfmArtistServiceImpl implements LastfmArtistService {

    private final LastfmArtistRepository artistRepository;

    public LastfmArtistServiceImpl(LastfmArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Override
    public List<LastfmArtist> findArtistsForGetInfo() {
        return artistRepository.findAllToGetInfoFor();
    }

}
