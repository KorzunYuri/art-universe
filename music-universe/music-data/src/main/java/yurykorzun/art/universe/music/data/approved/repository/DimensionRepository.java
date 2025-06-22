package yurykorzun.art.universe.music.data.approved.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.approved.entity.Dimension;

@Repository
public interface DimensionRepository extends JpaRepository<Dimension, Long> {
}
