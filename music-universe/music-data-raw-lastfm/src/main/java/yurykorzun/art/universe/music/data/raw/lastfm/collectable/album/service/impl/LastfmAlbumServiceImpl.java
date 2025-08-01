package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.LastfmAlbumService;

import java.util.List;
import java.util.Optional;

@Service
public class LastfmAlbumServiceImpl implements LastfmAlbumService {

    private final LastfmAlbumRepository albumRepository;

    public LastfmAlbumServiceImpl(LastfmAlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    @Override
    public List<LastfmAlbum> findAllByUrls(List<String> urls) {
        return albumRepository.findAllByUrlIn(urls);
    }

    @Override
    public Optional<LastfmAlbum> findById(Long entityId) {
        return albumRepository.findById(entityId);
    }

    @Override
    public List<LastfmAlbum> findEntitiesByUniqueKeys(List<String> uniqueKeys) {
        return findAllByUrls(uniqueKeys);
    }

    @Override
    public List<LastfmAlbum> saveAll(List<LastfmAlbum> lastfmAlbums) {
        return albumRepository.saveAll(lastfmAlbums);
    }

}
