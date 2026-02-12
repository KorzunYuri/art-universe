package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.RelationType;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelationTypeRepository extends JpaRepository<RelationType, Long> {

    Optional<RelationType> findByName(String name);

    List<RelationType> findByNameContainingIgnoreCase(String search);
}
