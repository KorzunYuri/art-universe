package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service;


import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;

import java.util.List;

public interface LastfmArtistService {
    
    LastfmArtist saveArtist(LastfmArtist artist);

    List<LastfmArtist> saveArtists(List<LastfmArtist> artists);

    List<LastfmArtist> findAllByNames(List<String> names);
    
}
