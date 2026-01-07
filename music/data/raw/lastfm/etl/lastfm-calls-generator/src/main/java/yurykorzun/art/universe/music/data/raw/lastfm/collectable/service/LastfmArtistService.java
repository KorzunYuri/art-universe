package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;

import java.util.List;

public interface LastfmArtistService {

    List<LastfmArtist> findArtistsForGetInfo();

}
