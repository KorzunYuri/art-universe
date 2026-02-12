package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.AlbumAlbum;

@Repository
public interface AlbumAlbumRepository extends JpaRepository<AlbumAlbum, Long> {
}
