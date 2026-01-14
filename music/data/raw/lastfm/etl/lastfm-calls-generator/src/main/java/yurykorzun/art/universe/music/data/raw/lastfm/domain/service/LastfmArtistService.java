package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;

import java.util.List;

public interface LastfmArtistService {

    List<LastfmArtist> findArtistsForGetInfo();

}
