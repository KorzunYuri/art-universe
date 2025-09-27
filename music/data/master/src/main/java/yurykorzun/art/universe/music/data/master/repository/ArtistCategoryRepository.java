package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.ArtistCategory;

import java.util.Optional;

@Repository
public interface ArtistCategoryRepository extends JpaRepository<ArtistCategory, Long> {
    
    boolean existsByArtistIdAndCategoryId(Long artistId, Long categoryId);
    
    Optional<ArtistCategory> findByArtistIdAndCategoryId(Long artistId, Long categoryId);
}
