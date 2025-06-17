package yurykorzun.art.universe.music.data.approved.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.approved.entity.Artist;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
}
