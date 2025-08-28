package yurykorzun.art.universe.music.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.quiz.entity.Track;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
    
    Optional<Track> findByMasterId(Long masterId);
    
    @Query("SELECT t FROM track t WHERE t.masterId IN :masterIds")
    List<Track> findByMasterIdIn(@Param("masterIds") List<Long> masterIds);
    
    void deleteByMasterId(Long masterId);
}
