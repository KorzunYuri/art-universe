package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;

import java.util.List;
import java.util.Optional;

@Service
public class LastfmArtistServiceImpl implements LastfmArtistService {

    private final LastfmArtistRepository artistRepository;

    public LastfmArtistServiceImpl(LastfmArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Override
    public LastfmArtist saveArtist(LastfmArtist artist) {
        return artistRepository.save(artist);
    }

    @Override
    public List<LastfmArtist> saveArtists(List<LastfmArtist> artists) {
        return artistRepository.saveAll(artists);
    }

    @Override
    public Optional<LastfmArtist> findByName(String name) {
        return artistRepository.findByName(name);
    }

    @Override
    public List<LastfmArtist> findAllByNames(List<String> names) {
        return artistRepository.findAllByNameIn(names);
    }

    @Override
    public List<LastfmArtist> findAllToGetInfoFor() {
        return artistRepository.findAllToGetInfoFor();
    }
}
