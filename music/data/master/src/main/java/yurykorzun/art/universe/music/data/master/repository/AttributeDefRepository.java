package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeDef;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeTargetType;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeDefRepository extends JpaRepository<AttributeDef, Long> {

    Optional<AttributeDef> findByCode(String code);

    @Query("""
        SELECT DISTINCT d FROM attribute_def d
        LEFT JOIN FETCH d.applicabilities a
        WHERE a.targetType = :targetType
        ORDER BY d.name
        """)
    List<AttributeDef> findByTargetType(@Param("targetType") AttributeTargetType targetType);

    @Query("""
        SELECT DISTINCT d FROM attribute_def d
        LEFT JOIN FETCH d.applicabilities
        ORDER BY d.name
        """)
    List<AttributeDef> findAllWithApplicabilities();
}
