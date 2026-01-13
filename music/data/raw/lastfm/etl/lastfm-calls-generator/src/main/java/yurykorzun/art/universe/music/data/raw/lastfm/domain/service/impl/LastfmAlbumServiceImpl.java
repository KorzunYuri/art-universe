package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmAlbumService;

import java.util.*;

@Service
public class LastfmAlbumServiceImpl implements LastfmAlbumService {

    private final LastfmAlbumRepository albumRepository;

    public LastfmAlbumServiceImpl(LastfmAlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    @Override
    public List<LastfmAlbum> findAlbumsForGetInfo() {
        return albumRepository.findAlbumsForGetInfo();
    }

}
