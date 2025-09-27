package yurykorzun.art.universe.music.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.quiz.entity.Artist;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    
    Optional<Artist> findByMasterId(Long masterId);
    
    @Query("SELECT a FROM artist a WHERE a.masterId IN :masterIds")
    List<Artist> findByMasterIdIn(@Param("masterIds") List<Long> masterIds);
    
    void deleteByMasterId(Long masterId);
}
