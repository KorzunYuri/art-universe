package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;

import java.util.List;

@Repository
public interface LastfmAlbumRepository extends JpaRepository<LastfmAlbum, Long> {

    List<LastfmAlbum> findAllByUrlIn(List<String> urls);

}
