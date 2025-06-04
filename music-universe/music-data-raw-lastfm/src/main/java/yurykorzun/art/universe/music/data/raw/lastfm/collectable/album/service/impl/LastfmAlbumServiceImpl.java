package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.LastfmAlbumService;

import java.util.List;

@Service
public class LastfmAlbumServiceImpl implements LastfmAlbumService {

    private final LastfmAlbumRepository lastfmAlbumRepository;

    public LastfmAlbumServiceImpl(LastfmAlbumRepository lastfmAlbumRepository) {
        this.lastfmAlbumRepository = lastfmAlbumRepository;
    }

    @Override
    public List<LastfmAlbum> findAllByUrls(List<String> urls) {
        return lastfmAlbumRepository.findAllByUrlIn(urls);
    }

    @Override
    public List<LastfmAlbum> saveAlbums(List<LastfmAlbum> lastfmAlbums) {
        return lastfmAlbumRepository.saveAll(lastfmAlbums);
    }

}
