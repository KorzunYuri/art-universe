package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.AlbumCategory;

import java.util.Optional;

@Repository
public interface AlbumCategoryRepository extends JpaRepository<AlbumCategory, Long> {

    boolean existsByAlbumIdAndCategoryId(Long albumId, Long categoryId);

    Optional<AlbumCategory> findByAlbumIdAndCategoryId(Long albumId, Long categoryId);
}
