package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.entity.RelationTypeApplicability;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelationTypeApplicabilityRepository extends JpaRepository<RelationTypeApplicability, Long> {

    List<RelationTypeApplicability> findByRelationTypeId(Long relationTypeId);

    List<RelationTypeApplicability> findBySourceEntityTypeAndTargetEntityType(
        MasterEntityType sourceEntityType, MasterEntityType targetEntityType);

    boolean existsByRelationTypeIdAndSourceEntityTypeAndTargetEntityType(
        Long relationTypeId, MasterEntityType sourceEntityType, MasterEntityType targetEntityType);

    Optional<RelationTypeApplicability> findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(
        MasterEntityType sourceEntityType, MasterEntityType targetEntityType);
}
